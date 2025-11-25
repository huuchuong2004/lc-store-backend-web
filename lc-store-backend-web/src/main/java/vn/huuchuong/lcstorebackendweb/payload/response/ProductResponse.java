package vn.huuchuong.lcstorebackendweb.payload.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Integer productId;

    private String name;
    private String description;
    private BigDecimal baseprice;

    private Integer categoryId;
    private String categoryName;

    private List<String> imageUrls;
    private List<ProductVariantResponse> variants;
}

