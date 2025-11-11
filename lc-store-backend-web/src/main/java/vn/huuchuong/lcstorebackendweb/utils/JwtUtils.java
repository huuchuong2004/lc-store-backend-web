package vn.huuchuong.lcstorebackendweb.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.huuchuong.lcstorebackendweb.payload.response.LoginUserResponse;


import javax.crypto.SecretKey;
import java.util.*;

public class JwtUtils {

    // Có thể chuyển sang application.properties và inject, ở đây để đơn giản:
    private static final String SECRET_BASE64 = System.getProperty("jwt.secret-base64",
            "kRZtO5/vUdtnabWGyd/N0CUU7h7ID4OK/OkxXi+j3Qxf7SV40PASQovBDnTIGAe4nSuonLwClVnwP1ucioXhFw==");

    // 5 phút (access token)
    private static final long ACCESS_TOKEN_EXP =
            Long.getLong("jwt.access-exp-ms", 5 * 60 * 1000L);

    // 7 ngày (refresh token)
    private static final long REFRESH_TOKEN_EXP =
            Long.getLong("jwt.refresh-exp-ms", 7L * 24 * 60 * 60 * 1000L);

    private static final SecretKey KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_BASE64));

    // 🔹 Tạo ACCESS TOKEN
    public static String createAccessToken(LoginUserResponse account, HttpServletRequest req) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ACCESS_TOKEN_EXP);
        String userAgent = req.getHeader("User-Agent");

        return Jwts.builder()
                .setId(String.valueOf(account.getId()))
                .setSubject(account.getUsername())
                .setIssuer("LC-Store")
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("role", account.getRole().name())
                .claim("user-agent", userAgent)
                .claim("type", "ACCESS")
                .signWith(KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    //  Tạo REFRESH TOKEN
    public static String createRefreshToken(LoginUserResponse account, HttpServletRequest req) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + REFRESH_TOKEN_EXP);
        String userAgent = req.getHeader("User-Agent");

        return Jwts.builder()
                .setId(String.valueOf(account.getId()))
                .setSubject(account.getUsername())
                .setIssuer("LC-Store")
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("role", account.getRole().name())
                .claim("user-agent", userAgent)
                .claim("type", "REFRESH")
                .signWith(KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // 🔹 Dành cho FILTER – chỉ check ACCESS token
    public static UsernamePasswordAuthenticationToken checkAccessToken(String token,
                                                                       HttpServletRequest req) {
        try {
            if (StringUtils.isBlank(token)) {
                System.err.println("Không có token");
                return null;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = (String) claims.get("type");
            if (!"ACCESS".equals(type)) {
                System.err.println("Token không phải ACCESS");
                return null;
            }

            Date exp = claims.getExpiration();
            if (exp == null || exp.before(new Date())) {
                System.err.println("Access token hết hạn");
                return null;
            }

            String uaPrev = String.valueOf(claims.get("user-agent"));
            String uaNow = req.getHeader("User-Agent");
            if (!Objects.equals(uaPrev, uaNow)) {
                System.err.println("User-Agent khác – yêu cầu đăng nhập lại");
                return null;
            }

            String username = claims.getSubject();
            String roleStr = String.valueOf(claims.get("role"));

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));

            return new UsernamePasswordAuthenticationToken(username, null, authorities);

        } catch (JwtException e) {
            System.err.println("Token không hợp lệ: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Lỗi khác khi kiểm tra token");
            e.printStackTrace();
            return null;
        }
    }

    // 🔹 Dùng cho /refresh – parse REFRESH token, ném exception nếu lỗi
    public static Claims parseRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = (String) claims.get("type");
            if (!"REFRESH".equals(type)) {
                throw new RuntimeException("Không phải refresh token");
            }

            Date exp = claims.getExpiration();
            if (exp == null || exp.before(new Date())) {
                throw new RuntimeException("Refresh token đã hết hạn");
            }

            return claims;
        } catch (JwtException e) {
            throw new RuntimeException("Refresh token không hợp lệ: " + e.getMessage());
        }
    }
}
