package vn.huuchuong.lcstorebackendweb.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.huuchuong.lcstorebackendweb.base.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "users",
        indexes = @Index(name = "idx_users_email", columnList = "email"), // truy van login sieu nhanh
        uniqueConstraints = {@UniqueConstraint(name = "uq_users_email", columnNames = "email") // dam bao ko co 2 user nao trugn email
                ,  @UniqueConstraint(name = "uq_users_username", columnNames = "username"),@UniqueConstraint(name="uq_users_password",columnNames = "password") }// ngan trung du lieu
)
public class User extends BaseEntity {
    // neu ke thua toi basse thi usser ko can phai co createat ma tu dong co , va tu dong trong bang sser se co 2 dong ben baseentity


    // Chỉ user dùng UUID
    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)", length = 36, updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @NotBlank
    @Column(name = "username",nullable = false)
    private String username;

    @PrePersist // truoc khi duoc luu vao du lieu thi thuc hien dong nay
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @Email
    @NotBlank
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank
    @Column(name="password" ,nullable = false, length = 255)
    private String password;

    @Column(length = 150)
    private String firstName;

    @Column(length = 150)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false)

    @Builder.Default
    private Boolean isActive = false;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)

    @Builder.Default
    private Role role = Role.USER; // default is user
}


