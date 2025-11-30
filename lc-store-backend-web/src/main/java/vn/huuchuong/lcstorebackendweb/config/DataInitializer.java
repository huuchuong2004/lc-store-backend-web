package vn.huuchuong.lcstorebackendweb.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.huuchuong.lcstorebackendweb.entity.Role;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.repository.IUserRepository;


import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDefaultUsers(IUserRepository userRepository) {
        return args -> {

            // TÀI KHOẢN ADMIN MẶC ĐỊNH
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("adminnek")
                        .email("adminnek@lcstore.com")
                        .password(passwordEncoder.encode("Admin@123")) // nhớ mã hóa
                        .firstName("LC")
                        .lastName("Admin")
                        .phone("0123456789")
                        .amount(BigDecimal.ZERO)
                        .isActive(true)
                        .role(Role.ADMIN)
                        .build();

                userRepository.save(admin);
            }

            // TÀI KHOẢN USER MẶC ĐỊNH
            if (!userRepository.existsByUsername("usernek")) {
                User user = User.builder()
                        .username("usernek")
                        .email("usernrk@lcstore.com")
                        .password(passwordEncoder.encode("User@123"))
                        .firstName("LC")
                        .lastName("User")
                        .phone("0987654321")
                        .amount(BigDecimal.ZERO)
                        .isActive(true)
                        .role(Role.USER) // default cũng là USER nhưng ghi rõ cho dễ đọc
                        .build();

                userRepository.save(user);
            }
        };
    }
}
