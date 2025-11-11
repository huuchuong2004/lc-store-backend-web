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

        String path = req.getRequestURI();

        // Các path không cần auth
        if (StringUtils.containsAnyIgnoreCase(path,
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/api/v1/auth/refresh",
                "/api/v1/account/create",
                "/api/v1/product/search",
                "/api/v1/auth/active",
                "/swagger-ui",
                "/swagger-resources",
                "/v3/api-docs"
        )) {
            chain.doFilter(req, res);
            return;
        }

        // Lấy token từ header Authorization: Bearer xxx
        String header = req.getHeader(AUTHORIZATION);
        String token = null;
        if (StringUtils.isNotBlank(header) &&
                header.toLowerCase().startsWith("bearer ")) {
            token = header.substring(7).trim();
        }

        var authentication = JwtUtils.checkAccessToken(token, req);
        if (authentication != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(req, res);
    }
}
