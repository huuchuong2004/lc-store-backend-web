package vn.huuchuong.lcstorebackendweb.service;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.payload.request.RefreshTokenRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.CreateUserRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.LoginRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.AuthResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.CreateUserResponse;

import java.util.UUID;


public interface IAuthService {

    BaseResponse<AuthResponse> login(LoginRequest request, HttpServletRequest httpReq);

    BaseResponse<AuthResponse> refresh(RefreshTokenRequest request, HttpServletRequest httpReq);

    BaseResponse<String> logout(Authentication authentication);
    BaseResponse<CreateUserResponse> register(CreateUserRequest request);

    String activateAccount(UUID accountId);
    BaseResponse<String> resendActivationEmail(String email);
}

