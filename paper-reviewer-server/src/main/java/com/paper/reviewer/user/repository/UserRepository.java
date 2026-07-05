package com.paper.reviewer.user.repository;

import com.paper.reviewer.user.domain.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    User save(User user);
}
