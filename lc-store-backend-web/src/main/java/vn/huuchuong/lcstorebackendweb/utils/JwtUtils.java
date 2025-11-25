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

    private static final String SECRET_BASE64 = System.getProperty("jwt.secret-base64",
            "kRZtO5/vUdtnabWGyd/N0CUU7h7ID4OK/OkxXi+j3Qxf7SV40PASQovBDnTIGAe4nSuonLwClVnwP1ucioXhFw==");

    private static final long ACCESS_TOKEN_EXP =
            Long.getLong("jwt.access-exp-ms", 100 * 60 * 1000L);   // 5 phút sua 100 thanh 5
    private static final long REFRESH_TOKEN_EXP =
            Long.getLong("jwt.refresh-exp-ms", 7L * 24 * 60 * 60 * 1000L); // 7 ngày

    private static final SecretKey KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_BASE64));

    // 🔹 Chuẩn hoá UA thành "browser|os"
    private static String normalizeUA(String ua) {
        if (ua == null) return "unknown";
        String u = ua.toLowerCase();

        String browser = "other";
        if (u.contains("chrome/") || u.contains("crios/") || u.contains("edg/")) browser = "chromium";
        else if (u.contains("firefox/")) browser = "firefox";
        else if (u.contains("safari/") && !u.contains("chrome/")) browser = "safari";

        String os = "other";
        if (u.contains("windows")) os = "windows";
        else if (u.contains("android")) os = "android";
        else if (u.contains("iphone") || u.contains("ipad") || u.contains("ios")) os = "ios";
        else if (u.contains("mac os") || u.contains("macintosh")) os = "macos";
        else if (u.contains("linux")) os = "linux";

        return browser + "|" + os;
    }

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

    // 🔹 Tạo REFRESH TOKEN
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

    // 🔹 Kiểm tra ACCESS TOKEN (so sánh UA nới lỏng)
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

    // 🔹 Dành cho /refresh – parse REFRESH token
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
