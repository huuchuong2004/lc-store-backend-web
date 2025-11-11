package vn.huuchuong.lcstorebackendweb.controller;



import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.payload.request.RefreshTokenRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.CreateUserRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.user.LoginRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.AuthResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.CreateUserResponse;
import vn.huuchuong.lcstorebackendweb.service.IAuthService;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

public class AuthController {

    private final IAuthService authService;

    @Transactional
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpReq) {

        BaseResponse<AuthResponse> response = authService.login(request, httpReq);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpReq) {

        BaseResponse<AuthResponse> response = authService.refresh(request, httpReq);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(Authentication authentication) {
        BaseResponse<String> response = authService.logout(authentication);
        return ResponseEntity.ok(response);
    }

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<CreateUserResponse>> register(
             @RequestBody @Valid CreateUserRequest request) {

        BaseResponse<CreateUserResponse> response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }



    @GetMapping(value = "/active/{accountId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> activate(@PathVariable("accountId") UUID accountId) {
        String html = authService.activateAccount(accountId);
        return ResponseEntity.ok(html);
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<BaseResponse<String>> resendActivation(@RequestParam String email) {
        BaseResponse<String> response = authService.resendActivationEmail(email);
        return ResponseEntity.ok(response);
    }

}


