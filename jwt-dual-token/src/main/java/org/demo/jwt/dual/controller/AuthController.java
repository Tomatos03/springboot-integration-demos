package org.demo.jwt.dual.controller;

import lombok.RequiredArgsConstructor;
import org.demo.jwt.dual.dto.LoginRequest;
import org.demo.jwt.dual.dto.RefreshTokenRequest;
import org.demo.jwt.dual.dto.Result;
import org.demo.jwt.dual.dto.TokenPair;
import org.demo.jwt.dual.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录签发双 token、刷新轮换、登出吊销。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<TokenPair> login(@RequestBody(required = false) LoginRequest request) {
        LoginRequest actual = request == null ? new LoginRequest(null, null) : request;
        return Result.success(authService.login(actual.userId(), actual.username()));
    }

    @PostMapping("/refresh")
    public Result<TokenPair> refresh(@RequestBody(required = false) RefreshTokenRequest request) {
        return Result.success(authService.refresh(request == null ? null : request.refreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
        return Result.success("已登出", null);
    }
}
