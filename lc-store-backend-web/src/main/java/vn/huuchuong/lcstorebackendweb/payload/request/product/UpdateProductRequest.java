package vn.huuchuong.lcstorebackendweb.payload.request.product;



import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

    // tất cả field đều optional – chỉ update nếu có giá trị
    private Integer categoryId;
    private String name;
    private String description;
    private BigDecimal baseprice;
}

