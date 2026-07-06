package com.paper.reviewer.review.infrastructure.persistence;

import com.paper.reviewer.review.repository.ReviewReportRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.review.infrastructure.persistence.ReviewReportEntity;
import com.paper.reviewer.review.infrastructure.persistence.ReviewReportMapper;
import com.paper.reviewer.review.domain.ReviewReport;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MyBatisReviewReportRepository implements ReviewReportRepository {
    private final ReviewReportMapper mapper;
    public MyBatisReviewReportRepository(ReviewReportMapper mapper) { this.mapper = mapper; }

    @Override public ReviewReport save(ReviewReport report) {
        ReviewReportEntity e = new ReviewReportEntity();
        e.setId(report.id()); e.setReviewId(report.reviewId()); e.setReviewerRole(report.reviewerRole());
        e.setContentMarkdown(report.contentMarkdown()); e.setScoresJson(report.scores());
        e.setStatus(report.status()); e.setCreatedAt(report.createdAt()); e.setUpdatedAt(report.updatedAt());
        if (report.id() == null) mapper.insert(e); else mapper.updateById(e);
        return toDomain(e);
    }

    @Override public List<ReviewReport> findByReviewId(long reviewId) {
        return mapper.selectList(new LambdaQueryWrapper<ReviewReportEntity>()
                .eq(ReviewReportEntity::getReviewId, reviewId).orderByAsc(ReviewReportEntity::getId))
                .stream().map(this::toDomain).toList();
    }

    private ReviewReport toDomain(ReviewReportEntity e) {
        return new ReviewReport(e.getId(), e.getReviewId(), e.getReviewerRole(), e.getContentMarkdown(),
                e.getScoresJson(), e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
