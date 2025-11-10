package vn.huuchuong.lcstorebackendweb.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Bật CORS và gắn cấu hình từ CORSConfig
                .cors(CORSConfig.configCorsCustomizer())

                // ❌ Tắt CSRF (vì ta dùng JWT, không dùng session form login)
                .csrf(csrf -> csrf.disable())

                // ⚙️ Không dùng session, mỗi request tự xác thực (JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔐 Quy định quyền truy cập cho từng loại API
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập không cần đăng nhập
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/users/register",
                                "/api/v1/auth/**",
                                "/api/v1/auth0/**",
                                "/actuator/health",
                                "/active/**"
                        ).permitAll()

                        // ADMIN-only
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**", "/api/v1/categories/**", "/api/v1/categories")
                        .hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/**", "/api/v1/categories/**", "/api/v1/categories")
                        .hasAuthority("ROLE_ADMIN")

                        // ADMIN hoặc USER đều truy cập được
                        .requestMatchers("/api/v1/categories")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")

                        // Còn lại thì phải đăng nhập
                        .anyRequest().authenticated()
                )

                // ⚠️ Nếu không có token / sai quyền → trả lỗi 401
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                ));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
