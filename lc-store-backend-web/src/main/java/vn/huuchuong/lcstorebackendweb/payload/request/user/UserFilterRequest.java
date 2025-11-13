package vn.huuchuong.lcstorebackendweb.payload.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserFilterRequest {


    @NotBlank(message = "Username not null !")
    private String username;


    @Email(message = "Incorrect Email")
    private String email;          // null -> không cập nhật


    private String firstName;      // null -> không cập nhật


    private String lastName;       // null -> không cập nhật

    @Pattern(regexp = "^(0|\\+84)\\d{9,10}$",
            message = "Vui lòng nhập đúng định dạng số điện thoại Việt Nam")
    private String phone;
}
