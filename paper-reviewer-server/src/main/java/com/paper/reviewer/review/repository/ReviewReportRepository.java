package com.paper.reviewer.review.repository;

import com.paper.reviewer.review.domain.ReviewReport;
import java.util.List;

public interface ReviewReportRepository {
    ReviewReport save(ReviewReport report);
    List<ReviewReport> findByReviewId(long reviewId);
}
