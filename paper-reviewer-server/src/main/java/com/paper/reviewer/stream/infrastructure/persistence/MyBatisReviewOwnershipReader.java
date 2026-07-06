package com.paper.reviewer.stream.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.review.infrastructure.persistence.ReviewEntity;
import com.paper.reviewer.review.infrastructure.persistence.ReviewMapper;
import com.paper.reviewer.stream.service.ReviewOwnershipReader;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisReviewOwnershipReader implements ReviewOwnershipReader {
    private final ReviewMapper mapper;

    public MyBatisReviewOwnershipReader(ReviewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isOwnedBy(long reviewId, long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<ReviewEntity>()
                .eq(ReviewEntity::getId, reviewId)
                .eq(ReviewEntity::getUserId, userId)) > 0;
    }
}
