package vn.huuchuong.lcstorebackendweb.payload.request.catogory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 255, message = "Tên sản phẩm phải từ 2 đến 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @NotNull(message = "Giá gốc không được null")
    @Min(value = 0, message = "Giá gốc phải >= 0")
    private BigDecimal basePrice;

    @NotNull(message = "CategoryId không được null")
    private Integer categoryId;   // ✅ thêm field này

    @NotNull(message = "Danh sách biến thể (variants) không được null")
    @Size(min = 1, message = "Sản phẩm phải có ít nhất 1 variant")
    @Valid
    private List<ProductVariantRequest> variants;

}
