package vn.huuchuong.lcstorebackendweb.payload.request.order;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Integer orderId;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String status;
    private String couponCode;
    private BigDecimal discountValue;
    private List<OrderItemResponse> items;
}

