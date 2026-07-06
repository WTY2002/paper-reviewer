package com.paper.reviewer.auth.service;

import com.paper.reviewer.user.domain.User;

public record AuthResult(User user, String token) { }
