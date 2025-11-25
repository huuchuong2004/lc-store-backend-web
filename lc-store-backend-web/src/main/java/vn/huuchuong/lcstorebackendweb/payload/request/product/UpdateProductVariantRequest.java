package vn.huuchuong.lcstorebackendweb.payload.request.product;



import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductVariantRequest {

    private String size;
    private String color;
    private BigDecimal price;
    private Integer quantityInStock;
}

