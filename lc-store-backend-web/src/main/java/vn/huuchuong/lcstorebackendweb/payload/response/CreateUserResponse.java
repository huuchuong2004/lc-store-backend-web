package vn.huuchuong.lcstorebackendweb.payload.response;

import lombok.*;
import vn.huuchuong.lcstorebackendweb.entity.Role;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
    private Role role;
}
