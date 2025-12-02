package vn.huuchuong.lcstorebackendweb.payload.request.order;


import lombok.Builder;
import lombok.Data;
import vn.huuchuong.lcstorebackendweb.entity.Payment;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentMethodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Integer orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String status;

    private String couponCode;
    private BigDecimal discountValue;
    private List<OrderItemResponse> items;
}

