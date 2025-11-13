package vn.huuchuong.lcstorebackendweb.service.impl;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
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

        // 👉 Dùng ModelMapper để map User -> LoginUserResponse
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
            return BaseResponse.error("Username exiist");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return BaseResponse.error("Email exiist");
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
            // Đọc nội dung file HTML trong resources/templates
            Path filePath = Paths.get("src/main/resources/templates/activation.html");
            return Files.readString(filePath);
        } catch (IOException e) {

            return """
            <!doctype html><meta charset="utf-8">
            <title>Kích hoạt tài khoản</title>
            <body style="font-family:system-ui;padding:32px">
              <h2>✅ Kích hoạt tài khoản thành công</h2>
              <p>Bạn có thể đóng tab này và quay lại ứng dụng.</p>
            </body>
        """;
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


}
