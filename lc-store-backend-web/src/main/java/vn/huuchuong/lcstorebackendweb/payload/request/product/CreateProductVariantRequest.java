package vn.huuchuong.lcstorebackendweb.payload.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductVariantRequest {

    private String size;

    private String color;

    private BigDecimal price;

    private Integer quantityInStock;

    private String sku; // có thể để trống, service sẽ tự gen
}