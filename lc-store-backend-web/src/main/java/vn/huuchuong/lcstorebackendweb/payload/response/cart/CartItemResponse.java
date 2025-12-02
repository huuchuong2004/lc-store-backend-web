package vn.huuchuong.lcstorebackendweb.payload.response.cart;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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

    // New: list of image URLs for the product (may be empty)
    private List<String> images;
}
