package com.paper.reviewer.reviewerteam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.paper.reviewer.database.entity.ReviewEntity;
import com.paper.reviewer.database.mapper.ReviewMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class MyBatisReviewerTeamReviewAccess implements ReviewerTeamReviewAccess {
    private final ReviewMapper mapper;

    public MyBatisReviewerTeamReviewAccess(ReviewMapper mapper) { this.mapper = mapper; }

    @Override
    public Optional<ReviewState> findOwned(long reviewId, long userId) {
        return Optional.ofNullable(mapper.selectOne(base(reviewId).eq(ReviewEntity::getUserId, userId)))
                .map(this::state);
    }

    @Override
    public Optional<ReviewState> find(long reviewId) {
        return Optional.ofNullable(mapper.selectOne(base(reviewId))).map(this::state);
    }

    @Override
    public boolean transition(long reviewId, String expectedStatus, String newStatus) {
        return mapper.update(null, new LambdaUpdateWrapper<ReviewEntity>()
                .eq(ReviewEntity::getId, reviewId)
                .eq(ReviewEntity::getStatus, expectedStatus)
                .isNull(ReviewEntity::getDeletedAt)
                .set(ReviewEntity::getStatus, newStatus)
                .set(ReviewEntity::getUpdatedAt, LocalDateTime.now())) == 1;
    }

    private LambdaQueryWrapper<ReviewEntity> base(long reviewId) {
        return new LambdaQueryWrapper<ReviewEntity>().eq(ReviewEntity::getId, reviewId)
                .isNull(ReviewEntity::getDeletedAt);
    }

    private ReviewState state(ReviewEntity entity) {
        return new ReviewState(entity.getId(), entity.getUserId(), entity.getStatus());
    }
}
