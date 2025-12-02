package vn.huuchuong.lcstorebackendweb.payload.response;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateVnPayPaymentResponse {
    private Integer paymentId;
    private String payUrl;
}

