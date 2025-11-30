package vn.huuchuong.lcstorebackendweb.service;

import vn.huuchuong.lcstorebackendweb.payload.request.cart.AddCartItemRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.cart.UpdateCartItemRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.cart.CartResponse;

public interface ICartService {
    CartResponse getMyCart();

    CartResponse addItem(AddCartItemRequest request);

    CartResponse updateItem(Integer cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(Integer cartItemId);

    CartResponse clearMyCart();
}
