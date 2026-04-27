package vn.huuchuong.lcstorebackendweb.service.impl;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.RefreshToken;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.RefreshTokenRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.CreateUserRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.LoginRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.AuthResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.CreateUserResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.LoginUserResponse;
import vn.huuchuong.lcstorebackendweb.repository.IUserRepository;
import vn.huuchuong.lcstorebackendweb.service.IAuthService;
import vn.huuchuong.lcstorebackendweb.service.IMailSenderService;
import vn.huuchuong.lcstorebackendweb.utils.JwtUtils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final IMailSenderService mailSenderService;
    private final ModelMapper modelMapper;   // 👈 inject ModelMapper

    @Override
    public BaseResponse<AuthResponse> login(LoginRequest request, HttpServletRequest httpReq) {

        Optional<User> optUser = userRepository.findByUsername(request.getUsername()); //Tim kiem user
        if (optUser.isEmpty()) {
            return BaseResponse.error("Sai username hoặc password");
        }

        User user = optUser.get(); // phat hien user

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) { // phat hein user va kiem tra mat khau
            return BaseResponse.error("Sai username hoặc password");
        }


        LoginUserResponse account = modelMapper.map(user, LoginUserResponse.class);

        refreshTokenService.revokeByUsernameAndUserAgent(user.getUsername(), httpReq.getHeader("User-Agent")); // se xoa di rfresh token cu neu login

        String accessToken = JwtUtils.createAccessToken(account, httpReq); // tien hanh tao refersh va access token
        String refreshToken = JwtUtils.createRefreshToken(account, httpReq);

        refreshTokenService.create(
                user.getUsername(),
                refreshToken,
                httpReq.getHeader("User-Agent"),
                7L * 24 * 60 * 60 * 1000  // 7 ngày
        ); // tao

        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken);
        return BaseResponse.success(authResponse, "Đăng nhập thành công");
    }

    @Override
    public BaseResponse<AuthResponse> refresh(RefreshTokenRequest request, HttpServletRequest httpReq) {

        String refreshTokenStr = request.getRefreshToken();
        if (StringUtils.isBlank(refreshTokenStr)) {
            return BaseResponse.error("Thiếu refreshToken");
        }

        try {
            // Parse JWT – check signature, type, exp
            Claims claims = JwtUtils.parseRefreshToken(refreshTokenStr);

            // Check DB – tồn tại, chưa revoke, đúng UA
            RefreshToken stored = refreshTokenService.verify(
                    refreshTokenStr,
                    httpReq.getHeader("User-Agent")
            );

            String username = claims.getSubject(); // chinh la ussername

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // 👉 Dùng ModelMapper để map User -> LoginUserResponse
            LoginUserResponse account = modelMapper.map(user, LoginUserResponse.class);

            String newAccessToken = JwtUtils.createAccessToken(account, httpReq);

            AuthResponse authResponse = new AuthResponse(newAccessToken, refreshTokenStr);
            return BaseResponse.success(authResponse, "Refresh token thành công");

        } catch (RuntimeException e) {
            return BaseResponse.error(e.getMessage());
        }
    }

    @Override
    public BaseResponse<String> logout(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return BaseResponse.error("Không xác định được user");
        }

        String username = authentication.getName();
        refreshTokenService.revokeByUsername(username);

        return BaseResponse.success("OK", "Đã logout");
    }
    @Override
    public BaseResponse<CreateUserResponse> register(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Phone already exists");
        }

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(false);

        User savedUser = userRepository.save(user);

        CreateUserResponse response = modelMapper.map(savedUser, CreateUserResponse.class);

        // Tạo link kích hoạt
        String activationLink = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/auth/active/{accountId}")
                .buildAndExpand(savedUser.getId())
                .toUriString();

        // Gửi mail bằng hàm chuyên dụng
        BaseResponse<String> mailResult =
                mailSenderService.sendActivationEmail(savedUser.getEmail(), activationLink);

        String message;
        if (mailResult.getData() == null) {
            message = "Tạo tài khoản thành công nhưng gửi email kích hoạt thất bại: "
                    + mailResult.getMessage();
        } else {
            message = "Tạo tài khoản thành công! Vui lòng kiểm tra email để kích hoạt.";
        }

        return new BaseResponse<>(response, message);
    }

    @Override
    public String activateAccount(UUID accountId) {
        User user = userRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            user.setIsActive(true);
            userRepository.save(user);
        }

        try {
            ClassPathResource resource = new ClassPathResource("templates/activation.html");
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            return fallbackHtml();
        }

    }

    @Override
    public BaseResponse<String> resendActivationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản với email này"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            return BaseResponse.error("Tài khoản đã được kích hoạt, không cần gửi lại email.");
        }

        String activationLink = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/auth/active/{accountId}")
                .buildAndExpand(user.getId())
                .toUriString();

        BaseResponse<String> mailResult =
                mailSenderService.sendActivationEmail(user.getEmail(), activationLink);

        if (mailResult.getData() == null) {
            return BaseResponse.error("Gửi lại email kích hoạt thất bại: " + mailResult.getMessage());
        }

        return BaseResponse.success("Đã gửi lại email kích hoạt tới: " + user.getEmail(),
                "Gửi lại email kích hoạt thành công");
    }
    private String fallbackHtml() {
        return """
        <!doctype html>
                           <html lang="vi">
                           <head>
                               <meta charset="utf-8">
                               <meta name="viewport" content="width=device-width, initial-scale=1.0">
                               <title>Kích hoạt tài khoản thành công</title>
                               <style>
                                   /* Reset cơ bản */
                                   body {
                                       margin: 0;
                                       padding: 0;
                                       font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                                       background-color: #f3f4f6; /* Màu nền xám nhẹ */
                                       display: flex;
                                       justify-content: center;
                                       align-items: center;
                                       min-height: 100vh;
                                   }
                
                                   /* Thẻ chứa nội dung */
                                   .card {
                                       background: white;
                                       padding: 40px;
                                       border-radius: 16px;
                                       box-shadow: 0 10px 25px rgba(0,0,0,0.05); /* Đổ bóng mềm */
                                       text-align: center;
                                       max-width: 400px;
                                       width: 90%;
                                       transition: transform 0.3s ease;
                                   }
                
                                   .card:hover {
                                       transform: translateY(-5px);
                                   }
                
                                   /* Vòng tròn chứa icon */
                                   .icon-circle {
                                       width: 80px;
                                       height: 80px;
                                       background-color: #d1fae5; /* Xanh lá nhạt */
                                       border-radius: 50%;
                                       display: flex;
                                       justify-content: center;
                                       align-items: center;
                                       margin: 0 auto 24px;
                                   }
                
                                   /* Icon dấu tích */
                                   .checkmark {
                                       color: #10b981; /* Xanh lá đậm */
                                       font-size: 40px;
                                   }
                
                                   /* Tiêu đề */
                                   h2 {
                                       color: #111827;
                                       margin: 0 0 12px;
                                       font-size: 24px;
                                       font-weight: 700;
                                   }
                
                                   /* Nội dung text */
                                   p {
                                       color: #6b7280;
                                       line-height: 1.6;
                                       margin: 0 0 32px;
                                   }
                
                                   /* Nút bấm (Button) */
                                   .btn {
                                       display: inline-block;
                                       background-color: #10b981;
                                       color: white;
                                       text-decoration: none;
                                       padding: 12px 24px;
                                       border-radius: 8px;
                                       font-weight: 600;
                                       transition: background-color 0.2s;
                                       width: 100%; /* Full width trên mobile */
                                       box-sizing: border-box;
                                   }
                
                                   .btn:hover {
                                       background-color: #059669;
                                   }
                               </style>
                           </head>
                           <body>
                
                               <div class="card">
                                   <div class="icon-circle">
                                       <svg xmlns="http://www.w3.org/2000/svg" class="checkmark" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                                           <polyline points="20 6 9 17 4 12"></polyline>
                                       </svg>
                                   </div>
                                   <h2>Kích hoạt thành công!</h2>
                                   <p>Tài khoản của bạn đã sẵn sàng sử dụng. Bạn có thể đóng tab này hoặc quay lại ứng dụng để đăng nhập.</p>
                
                                   <a href="#" class="btn">Quay về trang chủ</a>
                               </div>
                
                           </body>
                           </html>
    """;
    }



}
