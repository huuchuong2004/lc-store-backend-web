package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.entity.*;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.order.CheckoutRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderItemResponse;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderResponse;
import vn.huuchuong.lcstorebackendweb.repository.*;
import vn.huuchuong.lcstorebackendweb.service.IOrderService;

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


    @Override
    public Page<Order> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
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

            LocalDate today = LocalDate.now();
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

            Inventory inv = inventoryRepository.findByProductVariant(variant)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tồn kho cho biến thể"));

            if (inv.getCurrentStockLevel() < ci.getQuantity()) {
                throw new BusinessException("Sản phẩm " + variant.getSku()
                        + " không đủ tồn kho. Còn " + inv.getCurrentStockLevel());
            }

            inv.setCurrentStockLevel(inv.getCurrentStockLevel() - ci.getQuantity());
            inv.setLastUpdate(LocalDate.now());
            inventoryRepository.save(inv);
        }

        // Tạo Order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDate.now());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus("PENDING");
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
        order.setStatus("CREATED");
        orderRepository.save(order);

        // Log coupon_usage
        if (coupon != null) {
            boolean exists = couponUsageRepository.existsByOrder(order);
            if (!exists) {
                CouponUsage usage = new CouponUsage();
                usage.setCoupon(coupon);
                usage.setUser(user);
                usage.setOrder(order);
                usage.setUsedAt(LocalDateTime.now());
                couponUsageRepository.save(usage);
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
    public List<OrderResponse> getMyOrders() {

        User user = getCurrentUser();

        List<Order> orders = orderRepository.findByUserFetchItems(user);

        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
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
                .status(order.getStatus())
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
}
