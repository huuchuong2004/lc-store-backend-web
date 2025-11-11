package vn.huuchuong.lcstorebackendweb.payload.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.huuchuong.lcstorebackendweb.entity.Role;

import java.util.UUID;
@Data

public class LoginUserResponse {

    private UUID id;
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 30, message = "Username từ 3-30 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username chỉ gồm chữ, số, ., _, -")
    private String username;



    private Role role;

    private String fullName;

    private String userAgent;

    private String token;
}
