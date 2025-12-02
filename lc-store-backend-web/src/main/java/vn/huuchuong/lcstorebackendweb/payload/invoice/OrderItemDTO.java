package vn.huuchuong.lcstorebackendweb.payload.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemDTO {

    private Integer orderItemId;

    private Integer productVariantId;
    private String productName;
    private String size;
    private String color;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;

}
