package vn.huuchuong.lcstorebackendweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.cart.AddCartItemRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.cart.UpdateCartItemRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.cart.CartResponse;
import vn.huuchuong.lcstorebackendweb.service.ICartService;
import vn.huuchuong.lcstorebackendweb.service.IUserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

    private final ICartService cartService;

    /**
     * 1. Lấy giỏ hàng của user hiện tại
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> getMyCart() {
        CartResponse res = cartService.getMyCart();
        return ResponseEntity.ok(BaseResponse.success(res, "Lấy giỏ hàng thành công"));
    }

    /**
     * 2. Thêm item vào giỏ
     * POST /api/cart/items
     */
    @PostMapping("/items")
    public ResponseEntity<BaseResponse<CartResponse>> addItem(
            @RequestBody AddCartItemRequest request) {

        CartResponse res = cartService.addItem(request);
        return ResponseEntity.ok(BaseResponse.success(res, "Thêm sản phẩm vào giỏ thành công"));
    }

    /**
     * 3. Cập nhật số lượng 1 item trong giỏ
     * PUT /api/cart/items/{cartItemId}
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<BaseResponse<CartResponse>> updateItem(
            @PathVariable Integer cartItemId,
            @RequestBody UpdateCartItemRequest request) {

        CartResponse res = cartService.updateItem(cartItemId, request);
        return ResponseEntity.ok(BaseResponse.success(res, "Cập nhật giỏ hàng thành công"));
    }

    /**
     * 4. Xóa 1 item khỏi giỏ
     * DELETE /api/cart/items/{cartItemId}
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<BaseResponse<CartResponse>> removeItem(
            @PathVariable Integer cartItemId) {

        CartResponse res = cartService.removeItem(cartItemId);
        return ResponseEntity.ok(BaseResponse.success(res, "Xóa sản phẩm khỏi giỏ thành công"));
    }

    /**
     * 5. Xóa sạch giỏ hàng
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<BaseResponse<CartResponse>> clearCart() {
        CartResponse res = cartService.clearMyCart();
        return ResponseEntity.ok(BaseResponse.success(res, "Xóa toàn bộ giỏ hàng thành công"));
    }




}
