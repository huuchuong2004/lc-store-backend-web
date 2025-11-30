package vn.huuchuong.lcstorebackendweb.payload.request.order;



import lombok.Data;

@Data
public class CheckoutRequest {

    private String shippingAddress;
    private String couponCode; // optional, có thể null
}
