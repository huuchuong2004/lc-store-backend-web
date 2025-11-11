package vn.huuchuong.lcstorebackendweb.payload.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {


    @Size(min = 6, max = 30,message = "Do dai username chua phu hop 6- 30 !")
    @NotBlank(message = "Username not null !")
    private String username;


    @Size(min = 6, max = 20,message = "Do dai password chua phu hop tu 6- 20!")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,20}$",
            message = "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường và 1 số"
    )

    @NotNull(message = "Password not null")
    private String password;
}
