package vn.huuchuong.lcstorebackendweb.payload.request.product;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

    private Integer categoryId;

    private String name;

    private String description;

    private BigDecimal baseprice;

    private List<CreateProductVariantRequest> variants;

    private List<String> imageUrls;
}
