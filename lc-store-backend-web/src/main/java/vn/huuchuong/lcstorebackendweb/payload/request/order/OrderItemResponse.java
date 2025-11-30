package vn.huuchuong.lcstorebackendweb.payload.request.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {

    private Integer orderItemId;
    private Integer productVariantId;
    private String productName;
    private String size;
    private String color;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal; // unitPrice * quantity - discount
}

