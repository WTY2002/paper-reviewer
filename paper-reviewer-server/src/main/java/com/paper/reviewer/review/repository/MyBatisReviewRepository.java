package com.paper.reviewer.review.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.database.entity.ReviewEntity;
import com.paper.reviewer.database.mapper.ReviewMapper;
import com.paper.reviewer.review.domain.Review;
import com.paper.reviewer.review.domain.ReviewStatus;
import com.paper.reviewer.review.domain.ReviewType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisReviewRepository implements ReviewRepository {
    private final ReviewMapper mapper;
    public MyBatisReviewRepository(ReviewMapper mapper) { this.mapper = mapper; }

    @Override public Review save(Review review) {
        ReviewEntity entity = toEntity(review);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override public Review update(Review review) {
        ReviewEntity entity = toEntity(review);
        if (mapper.updateById(entity) != 1) throw new IllegalStateException("Review no longer exists");
        return toDomain(entity);
    }

    @Override public Optional<Review> findOwnedById(long userId, long reviewId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ReviewEntity>()
                .eq(ReviewEntity::getId, reviewId).eq(ReviewEntity::getUserId, userId)))
                .map(this::toDomain);
    }

    @Override public List<Review> findAllOwnedBy(long userId) {
        return mapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
                .eq(ReviewEntity::getUserId, userId).orderByDesc(ReviewEntity::getCreatedAt))
                .stream().map(this::toDomain).toList();
    }

    @Override public boolean deleteOwnedById(long userId, long reviewId) {
        return mapper.hardDeleteOwned(userId, reviewId) == 1;
    }

    private ReviewEntity toEntity(Review review) {
        ReviewEntity e = new ReviewEntity();
        e.setId(review.id()); e.setUserId(review.userId()); e.setPaperId(review.paperId());
        e.setReviewType(review.reviewType().name()); e.setStatus(review.status().name());
        e.setSourceLanguage(review.sourceLanguage()); e.setOutputLanguage(review.outputLanguage());
        e.setFieldAnalysisJson(review.fieldAnalysis());
        e.setEditorialDecisionMarkdown(review.editorialDecisionMarkdown());
        e.setRevisionRoadmapMarkdown(review.revisionRoadmapMarkdown());
        e.setAuthorQuestionsMarkdown(review.authorQuestionsMarkdown()); e.setErrorMessage(review.errorMessage());
        e.setCreatedAt(review.createdAt()); e.setUpdatedAt(review.updatedAt());
        return e;
    }

    private Review toDomain(ReviewEntity e) {
        return new Review(e.getId(), e.getUserId(), e.getPaperId(), ReviewType.valueOf(e.getReviewType()),
                ReviewStatus.valueOf(e.getStatus()), e.getSourceLanguage(), e.getOutputLanguage(),
                e.getFieldAnalysisJson(), e.getEditorialDecisionMarkdown(), e.getRevisionRoadmapMarkdown(),
                e.getAuthorQuestionsMarkdown(), e.getErrorMessage(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
