package vn.huuchuong.lcstorebackendweb.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import vn.huuchuong.lcstorebackendweb.utils.JwtRequestFilter;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity // để @PreAuthorize hoạt động
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(CORSConfig.configCorsCustomizer())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/users/register",
                                "/api/v1/auth/**",
                                "/api/v1/auth0/**",
                                "/actuator/health",
                                "/active/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").hasAnyAuthority("ROLE_ADMIN","ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*").authenticated() // se cho phep dc token dc nao preautho di qua
                        .requestMatchers("/api/v1/users/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req,res,e)->{
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("""
                        {"success":false,"error":"UNAUTHORIZED","message":"Thiếu hoặc token không hợp lệ","path":"%s"}
                    """.formatted(req.getRequestURI()));
                        })
                        .accessDeniedHandler((req,res,e)->{
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("""
                        {"success":false,"error":"FORBIDDEN","message":"Không đủ quyền truy cập","path":"%s"}
                    """.formatted(req.getRequestURI()));
                        })
                );

        // 🔴 Quan trọng: đưa JWT filter vào trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtRequestFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

