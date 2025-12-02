package vn.huuchuong.lcstorebackendweb.payload.request.order;



import lombok.Data;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentMethodType;

@Data
public class CheckoutRequest {

    private String shippingAddress;
    private String couponCode; // optional, có thể null
    private PaymentMethodType paymentMethodType;
}
