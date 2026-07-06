package com.paper.reviewer.rereview.infrastructure.persistence;

import com.paper.reviewer.rereview.repository.ReReviewRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.rereview.infrastructure.persistence.RereviewEntity;
import com.paper.reviewer.rereview.infrastructure.persistence.RereviewMapper;
import com.paper.reviewer.rereview.domain.ReReview;
import com.paper.reviewer.rereview.domain.ReReviewStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisReReviewRepository implements ReReviewRepository {
    private final RereviewMapper mapper;

    public MyBatisReReviewRepository(RereviewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ReReview save(ReReview rereview) {
        RereviewEntity entity = toEntity(rereview);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public ReReview update(ReReview rereview) {
        RereviewEntity entity = toEntity(rereview);
        if (mapper.updateById(entity) != 1) throw new IllegalStateException("Re-review no longer exists");
        return toDomain(entity);
    }

    @Override
    public Optional<ReReview> findOwnedById(long userId, long rereviewId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<RereviewEntity>()
                        .eq(RereviewEntity::getId, rereviewId)
                        .eq(RereviewEntity::getUserId, userId)))
                .map(this::toDomain);
    }

    @Override
    public List<ReReview> findByOriginalReviewId(long userId, long reviewId) {
        return mapper.selectList(new LambdaQueryWrapper<RereviewEntity>()
                        .eq(RereviewEntity::getUserId, userId)
                        .eq(RereviewEntity::getOriginalReviewId, reviewId))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean deleteOwnedById(long userId, long rereviewId) {
        return mapper.hardDeleteOwned(userId, rereviewId) == 1;
    }

    private RereviewEntity toEntity(ReReview rereview) {
        RereviewEntity entity = new RereviewEntity();
        entity.setId(rereview.id());
        entity.setUserId(rereview.userId());
        entity.setOriginalReviewId(rereview.originalReviewId());
        entity.setRevisedPaperId(rereview.revisedPaperId());
        entity.setResponsePaperId(rereview.responsePaperId());
        entity.setOutputLanguage(rereview.outputLanguage());
        entity.setStatus(rereview.status().name());
        entity.setResultMarkdown(rereview.resultMarkdown());
        entity.setChecklistJson(rereview.checklistJson());
        entity.setErrorMessage(rereview.errorMessage());
        entity.setCreatedAt(rereview.createdAt());
        entity.setUpdatedAt(rereview.updatedAt());
        return entity;
    }

    private ReReview toDomain(RereviewEntity entity) {
        return new ReReview(entity.getId(), entity.getUserId(), entity.getOriginalReviewId(),
                entity.getRevisedPaperId(), entity.getResponsePaperId(), entity.getOutputLanguage(),
                ReReviewStatus.valueOf(entity.getStatus()), entity.getResultMarkdown(),
                entity.getChecklistJson(), entity.getErrorMessage(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
