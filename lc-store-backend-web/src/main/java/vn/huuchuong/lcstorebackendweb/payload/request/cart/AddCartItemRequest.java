package vn.huuchuong.lcstorebackendweb.payload.request.cart;



import lombok.Data;

@Data
public class AddCartItemRequest {

    private Integer productVariantId;
    private Integer quantity;
}
