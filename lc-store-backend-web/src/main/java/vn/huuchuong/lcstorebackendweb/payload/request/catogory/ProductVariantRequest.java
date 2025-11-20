package vn.huuchuong.lcstorebackendweb.payload.request.catogory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.huuchuong.lcstorebackendweb.entity.Category;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductVariantRequest {


    @NotBlank(message = "Giá variant không được null")
    @Min(value = 0, message = "Giá variant phải >= 0")
    private BigDecimal price;

    @NotBlank(message = "Số lượng tồn kho không được null")
    @Min(value = 0, message = "Số lượng tồn kho phải >= 0")
    private Integer quantityInStock;

    @NotBlank(message = "Danh sách thuộc tính không được null")
    @Size(min = 1, message = "Variant phải có ít nhất 1 thuộc tính (vd: Size, Color)")
    @Valid
    private List<VariantAttributeRequest> attributes;


}
