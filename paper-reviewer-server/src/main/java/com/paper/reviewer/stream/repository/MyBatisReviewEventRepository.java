package com.paper.reviewer.stream.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.database.entity.ReviewEventEntity;
import com.paper.reviewer.database.mapper.ReviewEventMapper;
import com.paper.reviewer.stream.domain.ReviewEvent;
import com.paper.reviewer.stream.domain.ReviewEventType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MyBatisReviewEventRepository implements ReviewEventRepository {
    private final ReviewEventMapper mapper;

    public MyBatisReviewEventRepository(ReviewEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void lockReview(long reviewId) {
        if (mapper.lockReview(reviewId) == null) {
            throw new IllegalArgumentException("Review does not exist: " + reviewId);
        }
    }

    @Override
    public long nextSequence(long reviewId) {
        ReviewEventEntity last = mapper.selectOne(new LambdaQueryWrapper<ReviewEventEntity>()
                .eq(ReviewEventEntity::getReviewId, reviewId)
                .orderByDesc(ReviewEventEntity::getSequenceNo)
                .last("LIMIT 1"));
        return last == null ? 1L : Math.addExact(last.getSequenceNo(), 1L);
    }

    @Override
    public ReviewEvent save(ReviewEvent event) {
        ReviewEventEntity entity = new ReviewEventEntity();
        entity.setReviewId(event.reviewId());
        entity.setEventType(event.type().name());
        entity.setStage(event.stage());
        entity.setReviewerRole(event.reviewerRole());
        entity.setSequenceNo(event.sequence());
        entity.setEventPayload(event.payload());
        entity.setCreatedAt(event.createdAt());
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public List<ReviewEvent> findByReviewId(long reviewId) {
        return mapper.selectList(new LambdaQueryWrapper<ReviewEventEntity>()
                        .eq(ReviewEventEntity::getReviewId, reviewId)
                        .orderByAsc(ReviewEventEntity::getSequenceNo))
                .stream().map(this::toDomain).toList();
    }

    private ReviewEvent toDomain(ReviewEventEntity entity) {
        return new ReviewEvent(entity.getId(), entity.getReviewId(),
                ReviewEventType.valueOf(entity.getEventType()), entity.getStage(), entity.getReviewerRole(),
                entity.getSequenceNo(), entity.getEventPayload(), entity.getCreatedAt());
    }
}
