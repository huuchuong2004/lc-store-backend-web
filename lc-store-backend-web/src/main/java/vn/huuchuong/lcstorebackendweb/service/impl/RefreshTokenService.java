package vn.huuchuong.lcstorebackendweb.service.impl;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.entity.RefreshToken;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.repository.RefreshTokenRepository;


import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void revokeByUsernameAndUserAgent(String username, String userAgent) {
        refreshTokenRepository.deleteByUsernameAndUserAgent(username, userAgent);
    }

    @Transactional
    public RefreshToken create(String username,
                               String token,
                               String userAgent,
                               long expireMs) {
        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(token)
                .userAgent(userAgent)
                .expiryDate(Instant.now().plusMillis(expireMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }


    public RefreshToken verify(String token, String currentUserAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        if (refreshToken.isRevoked()) {
            throw new BusinessException("Refresh token đã bị revoke");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token đã hết hạn");
        }

        if (!Objects.equals(refreshToken.getUserAgent(), currentUserAgent)) {
            throw new BusinessException("Thiết bị khác – yêu cầu login lại");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeByUsername(String username) { // logout
        // đơn giản là xóa hết refresh token theo username
        refreshTokenRepository.deleteByUsername(username);
    }
}
