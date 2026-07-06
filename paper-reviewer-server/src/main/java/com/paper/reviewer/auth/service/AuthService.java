package com.paper.reviewer.auth.service;

import com.paper.reviewer.auth.security.JwtTokenProvider;
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
    public AuthResult register(RegisterCommand request) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
        User user = new User(null, email, passwordEncoder.encode(request.password()), request.displayName().trim());
        try {
            User saved = userRepository.save(user);
            return new AuthResult(saved, tokenProvider.createToken(saved));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS,
                    ErrorCode.AUTH_EMAIL_ALREADY_EXISTS.getDefaultMessage(), exception);
        }
    }

    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw invalidCredentials();
        }
        return new AuthResult(user, tokenProvider.createToken(user));
    }

    @Transactional(readOnly = true)
    public User currentUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_TOKEN_INVALID));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }
}
