package vn.huuchuong.lcstorebackendweb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
        indexes = @Index(name = "idx_users_email", columnList = "email"),
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
                @UniqueConstraint(name="uq_users_password",columnNames = "password")
        }
)
public class User extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)", length = 36, updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @NotBlank
    @Column(name = "username", nullable = false)
    private String username;

    @Email
    @NotBlank
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank
    @Column(name = "password", nullable = false, length = 255)
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

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Role role = Role.USER;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Cart cart;
}



