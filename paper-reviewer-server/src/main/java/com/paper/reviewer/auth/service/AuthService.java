package com.paper.reviewer.auth.service;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.auth.security.JwtTokenProvider;
import com.paper.reviewer.auth.web.AuthResponse;
import com.paper.reviewer.auth.web.LoginRequest;
import com.paper.reviewer.auth.web.RegisterRequest;
import com.paper.reviewer.auth.web.UserResponse;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.user.domain.User;
import com.paper.reviewer.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
        User user = new User(null, email, passwordEncoder.encode(request.password()), request.displayName().trim());
        try {
            User saved = userRepository.save(user);
            return AuthResponse.of(saved, tokenProvider.createToken(saved));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS,
                    ErrorCode.AUTH_EMAIL_ALREADY_EXISTS.getDefaultMessage(), exception);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw invalidCredentials();
        }
        return AuthResponse.of(user, tokenProvider.createToken(user));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(AuthenticatedUser principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_TOKEN_INVALID));
        return UserResponse.of(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }
}
