package vn.huuchuong.lcstorebackendweb.payload.response;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.huuchuong.lcstorebackendweb.entity.Role;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoadUserResponse {




    private String username;


    private String email;




    private String firstName;


    private String lastName;


    private String phone;


    private BigDecimal amount;


    private Boolean isActive ;



    private Role role ;
}
