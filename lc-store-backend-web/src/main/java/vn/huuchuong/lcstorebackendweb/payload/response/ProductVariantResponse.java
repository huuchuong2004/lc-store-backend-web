package vn.huuchuong.lcstorebackendweb.payload.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {

    private Integer productVariantId;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer quantityInStock;
    private String sku;
}

