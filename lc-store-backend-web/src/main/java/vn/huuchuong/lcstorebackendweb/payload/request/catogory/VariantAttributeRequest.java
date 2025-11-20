package vn.huuchuong.lcstorebackendweb.payload.request.catogory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.huuchuong.lcstorebackendweb.entity.Category;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VariantAttributeRequest {

    @NotBlank(message = "Tên thuộc tính không được để trống (vd: Size, Color)")
    private String name;   // Ví dụ: "Size", "Color"

    @NotBlank(message = "Giá trị thuộc tính không được để trống (vd: L, Đen)")
    private String value;  // Ví dụ: "L", "Đen"
}
