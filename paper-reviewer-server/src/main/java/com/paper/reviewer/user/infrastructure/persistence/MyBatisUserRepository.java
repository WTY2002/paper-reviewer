package com.paper.reviewer.user.infrastructure.persistence;

import com.paper.reviewer.user.repository.UserRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.user.infrastructure.persistence.UserEntity;
import com.paper.reviewer.user.infrastructure.persistence.UserMapper;
import com.paper.reviewer.user.domain.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class MyBatisUserRepository implements UserRepository {
    private final UserMapper userMapper;

    public MyBatisUserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        UserEntity entity = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email));
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = new UserEntity();
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setDisplayName(user.displayName());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        userMapper.insert(entity);
        return toDomain(entity);
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getDisplayName());
    }
}
