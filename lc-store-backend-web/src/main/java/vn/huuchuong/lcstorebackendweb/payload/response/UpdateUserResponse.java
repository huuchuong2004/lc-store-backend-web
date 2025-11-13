package vn.huuchuong.lcstorebackendweb.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import vn.huuchuong.lcstorebackendweb.entity.Role;

import java.util.UUID;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // optional cho đẹp JSON
public class UpdateUserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
    private Role role;
}
