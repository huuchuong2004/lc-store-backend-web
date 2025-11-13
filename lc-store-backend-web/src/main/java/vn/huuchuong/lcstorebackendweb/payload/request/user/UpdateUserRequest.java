package vn.huuchuong.lcstorebackendweb.payload.request.user;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;
import vn.huuchuong.lcstorebackendweb.entity.Role;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class UpdateUserRequest {

    @Size(min = 6, max = 30, message = "Do dai email chua phu hop 6-30!")
    @Email(message = "Incorrect Email")
    private String email;          // null -> không cập nhật

    @Size(min = 2, max = 150, message = "Do dai chua phu hop")
    private String firstName;      // null -> không cập nhật

    @Size(min = 2, max = 150, message = "Do dai chua phu hop")
    private String lastName;       // null -> không cập nhật

    @Pattern(regexp = "^(0|\\+84)\\d{9,10}$",
            message = "Vui lòng nhập đúng định dạng số điện thoại Việt Nam")
    private String phone;


    private BigDecimal amount;


    private Boolean isActive ;


    private Role role ;
}
