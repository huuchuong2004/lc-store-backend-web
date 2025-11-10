package vn.huuchuong.lcstorebackendweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CORSConfig {

    public static Customizer<CorsConfigurer<HttpSecurity>> configCorsCustomizer() {
        return c -> {
            CorsConfigurationSource source = request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(
                        "http://localhost:3000",
                        "http://127.0.0.1:5500",
                        "http://localhost:5500",
                        "http://localhost:8080",
                        "http://localhost:8081",
                        "http://localhost:8082",
                        "http://localhost:8083"
                ));
                // nhớ thêm OPTIONS để preflight không lỗi
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);// co dong nay thi moi request se gui kem cookie , token....
                return config;
            };

            // gắn source vào CorsConfigurer
            c.configurationSource(source);
        };
    }
}
