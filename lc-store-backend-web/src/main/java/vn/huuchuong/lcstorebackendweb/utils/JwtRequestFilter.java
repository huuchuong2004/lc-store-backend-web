package vn.huuchuong.lcstorebackendweb.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.huuchuong.lcstorebackendweb.utils.JwtUtils;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    @NotNull HttpServletResponse res,
                                    @NotNull FilterChain chain) throws ServletException, IOException {


        String rawPath = req.getRequestURI();
        String path = rawPath == null ? "" : rawPath.toLowerCase(java.util.Locale.ROOT);

// Bỏ qua preflight CORS
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        boolean whitelisted =
                // Auth endpoints (login/register/activate/refresh/active...) — cho toàn bộ /api/v1/auth/**
                path.equals("/api/v1/auth/login")
                        || path.equals("/api/v1/auth/register")
                        || path.startsWith("/api/v1/auth/")   // covers /activate, /active, /refresh, v.v.
                        // Các endpoint public khác
                        || path.equals("/api/v1/account/create")
                        || path.equals("/api/v1/product/search")
                        // Swagger/OpenAPI
                        || path.startsWith("/swagger-ui")
                        || path.startsWith("/swagger-resources")
                        || path.startsWith("/v3/api-docs")
                        // Static/common
                        || path.equals("/favicon.ico")
                        || path.equals("/")
                ;

        if (whitelisted) {
            chain.doFilter(req, res);
            return;
        }


        String header = req.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {

            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7).trim();

// Chỉ khi CÓ token mới validate + set Authentication
        var authentication = JwtUtils.checkAccessToken(token, req);
        if (authentication != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(req, res);
    }
}
