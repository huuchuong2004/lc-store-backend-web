package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.entity.*;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.cart.AddCartItemRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.cart.UpdateCartItemRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.cart.CartItemResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.cart.CartResponse;
import vn.huuchuong.lcstorebackendweb.repository.*;
import vn.huuchuong.lcstorebackendweb.service.ICartService;
import vn.huuchuong.lcstorebackendweb.service.IUserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final IUserRepository userRepository;
    private final ICartItemRepository cartItemRepository;
    private final IProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;

    public Cart getOrCreateCart(User user) {

        // User đã có cart? → return
        if (user.getCart() != null) {
            return user.getCart();
        }

        // Chưa có → tạo mới
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDate.now().atStartOfDay());
        cart.setUpdatedAt(LocalDate.now().atStartOfDay());

        // Set vào user (để quan hệ 2 chiều hoạt động)
        user.setCart(cart);

        return cartRepository.save(cart);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName(); // lay tu token
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User không tồn tại"));

    }

    @Override
    public CartResponse getMyCart() {
        User u = getCurrentUser();
        Cart cart = getOrCreateCart(u);
        // Chuyển Cart entity sang CartResponse và trả về
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("Số lượng phải lớn hơn 0");
        }

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new BusinessException("Biến thể sản phẩm không tồn tại"));

        Inventory inventory = inventoryRepository.findByProductVariant(variant)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tồn kho cho biến thể"));

        if (inventory.getCurrentStockLevel() < request.getQuantity()) {
            throw new BusinessException("Chỉ còn " + inventory.getCurrentStockLevel() + " sản phẩm trong kho");
        }

        Optional<CartItem> exist = cartItemRepository.findByCartAndProductVariant(cart, variant);

        if (exist.isPresent()) {
            CartItem item = exist.get();
            int newQty = item.getQuantity() + request.getQuantity();

            if (newQty > inventory.getCurrentStockLevel()) {
                throw new BusinessException("Số lượng vượt quá tồn kho hiện tại");
            }

            item.setQuantity(newQty);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductVariant(variant);
            item.setQuantity(request.getQuantity());
            cart.getItems().add(item);
        }

        cart.setUpdatedAt(LocalDate.now().atStartOfDay());
        cartRepository.save(cart);

        // reload lại với fetch join cho chắc (tránh lazy + mapping)
        Cart reloaded = cartRepository.findByUserFetchItems(user)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giỏ hàng sau khi thêm"));

        return mapToCartResponse(reloaded);
    }

    @Override
    public CartResponse updateItem(Integer cartItemId, UpdateCartItemRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("Số lượng phải lớn hơn 0");
        }

        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("Sản phẩm trong giỏ không tồn tại"));
        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new BusinessException("Sản phẩm không thuộc giỏ hàng của bạn");
        }

        Inventory inventory = inventoryRepository.findByProductVariant(item.getProductVariant())
                .orElseThrow(() -> new BusinessException("Không tìm thấy tồn kho cho biến thể"));

        if (request.getQuantity() > inventory.getCurrentStockLevel()) {
            throw new BusinessException("Chỉ còn " + inventory.getCurrentStockLevel() + " sản phẩm trong kho");
        }
        item.setQuantity(request.getQuantity());
        cart.setUpdatedAt(LocalDate.now().atStartOfDay());
        cartRepository.save(cart);

        Cart reloaded = cartRepository.findByUserFetchItems(user)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giỏ hàng sau khi cập nhật"));
        return mapToCartResponse(reloaded);
    }

    @Override
    public CartResponse removeItem(Integer cartItemId) {
        if (cartItemId == null) {
            throw new BusinessException("cartItemId không được để trống");
        }
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("Sản phẩm trong giỏ không tồn tại"));
        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new BusinessException("Sản phẩm không thuộc giỏ hàng của bạn");
        }
        cart.getItems().remove(item);
        cart.setUpdatedAt(LocalDate.now().atStartOfDay());
        cartRepository.save(cart);
        Cart reloaded = cartRepository.findByUserFetchItems(user)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giỏ hàng sau khi xóa"));
        return mapToCartResponse(reloaded);
    }


        @Override
        public CartResponse clearMyCart () {

            User user = getCurrentUser();
            Cart cart = getOrCreateCart(user);

            cart.getItems().clear();
            cartItemRepository.deleteAllByCart(cart);
            cart.setUpdatedAt(LocalDate.now().atStartOfDay());
            cartRepository.save(cart);

            // cart lúc này không có item, fetch cũng ok
            Cart reloaded = cartRepository.findByUserFetchItems(user)
                    .orElseGet(() -> cart); // nếu không fetch được thì trả cart vừa clear

            return mapToCartResponse(reloaded);
        }



    private CartResponse mapToCartResponse(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;

        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            ProductVariant v = item.getProductVariant();
            BigDecimal subtotal = v.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            return CartItemResponse.builder()
                    .cartItemId(item.getCartItemId())
                    .productVariantId(v.getProductVariantId())
                    .productName(v.getProduct().getName())
                    .size(v.getSize())
                    .color(v.getColor())
                    .price(v.getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build();
        }).toList();

        for (CartItemResponse r : itemResponses) {
            total = total.add(r.getSubtotal());
        }

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(itemResponses)
                .totalAmount(total)
                .build();
    }

}
