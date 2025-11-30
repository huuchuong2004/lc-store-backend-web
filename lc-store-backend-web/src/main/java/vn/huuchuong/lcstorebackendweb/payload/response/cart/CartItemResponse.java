package vn.huuchuong.lcstorebackendweb.payload.response.cart;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {

    private Integer cartItemId;
    private Integer productVariantId;
    private String productName;
    private String size;
    private String color;
    private BigDecimal price;   // giá 1 cái (theo variant)
    private Integer quantity;
    private BigDecimal subtotal; // price * quantity
}

