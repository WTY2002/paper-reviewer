package com.paper.reviewer.auth.security;

public record AuthenticatedUser(Long userId, String email) {
}
