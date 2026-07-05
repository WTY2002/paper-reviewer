package com.paper.reviewer.auth.web;

import com.paper.reviewer.user.domain.User;

public record UserResponse(Long userId, String email, String displayName) {
    public static UserResponse of(User user) {
        return new UserResponse(user.id(), user.email(), user.displayName());
    }
}
