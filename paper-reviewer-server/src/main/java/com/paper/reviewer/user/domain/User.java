package com.paper.reviewer.user.domain;

public record User(Long id, String email, String passwordHash, String displayName) {
}
