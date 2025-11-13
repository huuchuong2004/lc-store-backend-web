package vn.huuchuong.lcstorebackendweb.payload.request.user;

import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
// Not Null chi ngan rong khong ngan "", Con NotBlank se bao gom ca 2 cai
public class CreateUserRequest {

    @Size(min = 6, max = 30,message = "Do dai username chua phu hop 6- 30 !")
    @NotBlank(message = "Username not null !")
    private String username;

    @Size(min = 6, max = 30,message = "Do dai email chua phu hop 6- 30!")
    @Email(message = "Incorrect Email")
    @NotBlank(message = "Email not null !")
    private String email;

    @Size(min = 6, max = 20,message = "Do dai password chua phu hop tu 6- 20!")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,20}$",
            message = "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường và 1 số"
    )

    @NotNull(message = "Password not null")
    private String password;

    @Column(length = 150)
    @Size(min = 2,message = "Do dai chua phu hop")
    @NotBlank(message = "First name not null !")
    private String firstName;

    @Column(length = 150)
    @Size(min = 2,message = "Do dai chua phu hop")
    @NotBlank(message = "last name not null !")
    private String lastName;

    @NotBlank(message = "Phone not null !")
    @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "Vui lòng nhập đúng định dạng số điện thoại Việt Nam")
    @Size(min = 9,message = "Vui long nhap dung dinhd dang SDT")
    private String phone;

}
