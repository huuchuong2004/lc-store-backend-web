package vn.huuchuong.lcstorebackendweb.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lưu username cho đơn giản (khỏi join user)
    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(length = 300)
    private String userAgent;

    @Column(nullable = false)
    private boolean revoked = false;
}

