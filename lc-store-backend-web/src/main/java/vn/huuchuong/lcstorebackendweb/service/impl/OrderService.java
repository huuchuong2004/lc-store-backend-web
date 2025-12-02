package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.entity.*;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.OrderStatus;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.order.CheckoutRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderItemResponse;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderResponse;
import vn.huuchuong.lcstorebackendweb.payload.request.order.UserOrderResponse;
import vn.huuchuong.lcstorebackendweb.repository.*;
import vn.huuchuong.lcstorebackendweb.service.IOrderService;
import java.time.ZoneId;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final IUserRepository userRepository;
    private final ICartRepository cartRepository;
    private final InventoryRepository inventoryRepository;
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ICouponRepository couponRepository;
    private final ICouponUsageRepository couponUsageRepository;
    private final IProductVariantRepository productVariantRepository;
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");


    @Override
    public Page<OrderResponse> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToOrderResponse);
    }

    @Override
    public OrderResponse checkout(CheckoutRequest request) {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUserFetchItems(user)
                .orElseThrow(() -> new BusinessException("Giỏ hàng trống"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("Giỏ hàng không có sản phẩm");
        }

        // Tính tổng tiền giỏ trước khi giảm giá
        BigDecimal cartTotal = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            BigDecimal price = ci.getProductVariant().getPrice();
            cartTotal = cartTotal.add(price.multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        Coupon coupon = null;
        BigDecimal discountValue = BigDecimal.ZERO;

        // Nếu có coupon code => validate
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {

            coupon = couponRepository.findByCouponCode(request.getCouponCode().trim())
                    .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại"));

            LocalDate today = LocalDate.now(APP_ZONE);
            if (coupon.getStartDate() != null && today.isBefore(coupon.getStartDate())) {
                throw new BusinessException("Mã giảm giá chưa đến thời gian sử dụng");
            }
            if (coupon.getEndDate() != null && today.isAfter(coupon.getEndDate())) {
                throw new BusinessException("Mã giảm giá đã hết hạn");
            }

            if (coupon.getMinimumOrderAmount() != null &&
                    cartTotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
                throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá");
            }

            long totalUsage = couponUsageRepository.countByCoupon(coupon);
            if (coupon.getMaxUsage() != null && totalUsage >= coupon.getMaxUsage()) {
                throw new BusinessException("Mã giảm giá đã hết lượt sử dụng");
            }

            long userUsage = couponUsageRepository.countByCouponAndUser(coupon, user);
            if (coupon.getMaxUsagePerUser() != null && userUsage >= coupon.getMaxUsagePerUser()) {
                throw new BusinessException("Bạn đã dùng hết số lần cho phép của mã này");
            }

            discountValue = coupon.getDiscountValue() != null ? coupon.getDiscountValue() : BigDecimal.ZERO;
        }

        // Kiểm tra tồn kho từng item + trừ kho
        for (CartItem ci : cart.getItems()) {

            ProductVariant variant = ci.getProductVariant();
            int qty = ci.getQuantity();

            Inventory inv = inventoryRepository.findByProductVariant(variant)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tồn kho cho biến thể"));

            int invStock = inv.getCurrentStockLevel() != null ? inv.getCurrentStockLevel() : 0;
            int variantStock = variant.getQuantityInStock() != null ? variant.getQuantityInStock() : 0;

            // Kiểm tra tồn kho (Inventory là nguồn chính)
            if (invStock < qty) {
                throw new BusinessException("Sản phẩm " + variant.getSku()
                        + " không đủ tồn kho. Còn " + invStock);
            }

            // 1. TRỪ tồn kho ở Inventory
            inv.setCurrentStockLevel(invStock - qty);
        inv.setLastUpdate(LocalDate.now(APP_ZONE));
            inventoryRepository.save(inv);

            // 2. TRỪ tồn kho ở ProductVariant
            variant.setQuantityInStock(variantStock - qty);
            productVariantRepository.save(variant);
        }


        // Tạo Order
        Order order = new Order();
        order.setUser(user);
    order.setOrderDate(LocalDateTime.now(APP_ZONE));
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setCoupon(coupon);


        // tạm thời set 0, lát nữa set lại
        order.setTotalAmount(BigDecimal.ZERO);

        orderRepository.save(order);

        // Tạo OrderItem từ CartItem
        BigDecimal totalAfter = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {

            ProductVariant v = ci.getProductVariant();
            BigDecimal unitPrice = v.getPrice();
            int qty = ci.getQuantity();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductVariant(v);
            oi.setQuantity(qty);
            oi.setUnitPrice(unitPrice);
            oi.setDiscountAmount(BigDecimal.ZERO); // có thể phân bổ discount sau

            orderItemRepository.save(oi);

            totalAfter = totalAfter.add(lineTotal);
        }

        // Áp discount cho tổng (đơn giản: trừ thẳng)
        if (discountValue != null && discountValue.compareTo(BigDecimal.ZERO) > 0) {
            totalAfter = totalAfter.subtract(discountValue);
            if (totalAfter.compareTo(BigDecimal.ZERO) < 0) {
                totalAfter = BigDecimal.ZERO;
            }
        }

        order.setTotalAmount(totalAfter);
        order.setStatus(OrderStatus.CREATED);
        orderRepository.save(order);

        // Log coupon_usage
        if (coupon != null) {
            boolean exists = couponUsageRepository.existsByOrder(order);
            if (!exists) {
                CouponUsage usage = new CouponUsage();
                usage.setCoupon(coupon);
                usage.setUser(user);
                usage.setOrder(order);
                usage.setUsedAt(LocalDateTime.now(APP_ZONE));
                couponUsageRepository.save(usage);

                Integer currentUsage = coupon.getCurrentUsage() != null ? coupon.getCurrentUsage() : 0;
                coupon.setCurrentUsage(currentUsage + 1);
                couponRepository.save(coupon); // Lưu lại thông tin Coupon đã cập nhật
            }
        }

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        // load lại order đầy đủ items (fetch join)
        Order reloaded = orderRepository.findByIdFetchItems(order.getOrderId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng sau khi tạo"));

        return mapToOrderResponse(reloaded);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer orderId) {

        User user = getCurrentUser();

        Order order = orderRepository.findByIdFetchItems(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền xem đơn hàng này");
        }

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {

        User user = getCurrentUser();

        Page<Order> orderPage = orderRepository.findByUserFetchItems(user, pageable);

        // map Page<Order> -> Page<OrderResponse>
        return orderPage.map(this::mapToOrderResponse);
    }


    @Transactional
    public OrderResponse cancelOrder(Integer orderId) {

        User user = getCurrentUser();
        boolean admin = isAdmin();

        Order order = orderRepository.findByIdFetchItems(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng"));

        // Nếu KHÔNG phải admin thì phải là chủ đơn
        if (!admin && !order.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bạn không có quyền huỷ đơn hàng này");
        }

        // Nếu KHÔNG phải admin thì chỉ được huỷ ở một số trạng thái
        if (!admin &&
                order.getStatus() != OrderStatus.CREATED &&
                order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Không thể huỷ đơn hàng ở trạng thái hiện tại");
        }

        // HOÀN TỒN KHO (admin hay user đều hoàn kho như nhau)
        for (OrderItem item : order.getItems()) {

            ProductVariant variant = item.getProductVariant();
            int qty = item.getQuantity();

            Inventory inv = inventoryRepository.findByProductVariant(variant)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tồn kho cho biến thể"));

            int invStock = inv.getCurrentStockLevel() != null ? inv.getCurrentStockLevel() : 0;
            int variantStock = variant.getQuantityInStock() != null ? variant.getQuantityInStock() : 0;

            // 1. Cộng tồn kho Inventory
            inv.setCurrentStockLevel(invStock + qty);
            inv.setLastUpdate(LocalDate.now(APP_ZONE));
            inventoryRepository.save(inv);

            // 2. Cộng tồn kho ProductVariant
            variant.setQuantityInStock(variantStock + qty);
            productVariantRepository.save(variant);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse getDetailsAdminRole(Integer orderId) {
        Order order = orderRepository.findByIdFetchItems(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));
        return mapToOrderResponse(order);
    }

    @Override
    public UserOrderResponse getUserByOrderId(Integer orderId) {
        Order order=orderRepository.findById(orderId).orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));
        User user=order.getUser();
        return UserOrderResponse.builder()
                .userId(user.getId())
                .orderId(orderId)
                .fisrtName(user.getFirstName())
                .username(user.getUsername())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }


    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(oi -> {
            ProductVariant v = oi.getProductVariant();
            BigDecimal lineTotal = oi.getUnitPrice()
                    .multiply(BigDecimal.valueOf(oi.getQuantity()))
                    .subtract(oi.getDiscountAmount() != null ? oi.getDiscountAmount() : BigDecimal.ZERO);

            return OrderItemResponse.builder()
                    .orderItemId(oi.getOrderItemId())
                    .productVariantId(v.getProductVariantId())
                    .productName(v.getProduct().getName())
                    .size(v.getSize())
                    .color(v.getColor())
                    .unitPrice(oi.getUnitPrice())
                    .quantity(oi.getQuantity())
                    .discountAmount(oi.getDiscountAmount())
                    .lineTotal(lineTotal)
                    .build();
        }).toList();

        BigDecimal discountValue = order.getCoupon() != null
                ? order.getCoupon().getDiscountValue()
                : BigDecimal.ZERO;

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus().name())
                .couponCode(order.getCoupon() != null ? order.getCoupon().getCouponCode() : null)
                .discountValue(discountValue)
                .items(itemResponses)
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));
    }
    private boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }


    public boolean setStatusIsShipping(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));
        order.setStatus(OrderStatus.SHIPPING);
        orderRepository.save(order);
        return true;
    }

    public boolean setStatusIsDelivered(Integer orderId) {
        Order order=orderRepository.findById(orderId).orElseThrow(()-> new BusinessException("Don Hang Khong Ton Tai"));
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
        return true;
    }
}
