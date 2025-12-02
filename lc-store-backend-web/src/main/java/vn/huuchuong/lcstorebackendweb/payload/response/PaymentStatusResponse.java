package vn.huuchuong.lcstorebackendweb.payload.response;

import lombok.Builder;
import lombok.Data;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentMethodType;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentStatus;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentStatusResponse {

    private Integer orderId;
    private Integer paymentId;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private BigDecimal amount;
}

