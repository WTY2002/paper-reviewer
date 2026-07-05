package com.paper.reviewer.auth.web;

import com.paper.reviewer.user.domain.User;

public record AuthResponse(Long userId, String email, String displayName, String token) {
    public static AuthResponse of(User user, String token) {
        return new AuthResponse(user.id(), user.email(), user.displayName(), token);
    }
}
