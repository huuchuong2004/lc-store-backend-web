package vn.huuchuong.lcstorebackendweb.payload.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilter {
    private String name;
    private Integer categoryId;

    // minPrice = giá thấp nhất
    // maxPrice = giá cao nhất
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
