package com.paper.reviewer.auth.web;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.auth.service.AuthService;
import com.paper.reviewer.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = authService.register(request.toCommand());
        return ApiResponse.success(AuthResponse.of(result.user(), result.token()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(request.toCommand());
        return ApiResponse.success(AuthResponse.of(result.user(), result.token()));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponse.success(UserResponse.of(authService.currentUser(principal.userId())));
    }
}
