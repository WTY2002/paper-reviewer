package com.paper.reviewer.export.repository;

import com.paper.reviewer.export.domain.ReviewExport;
import java.util.List;
import java.util.Optional;

public interface ExportRepository {
    ReviewExport save(ReviewExport export);
    Optional<ReviewExport> findOwnedById(long userId, long exportId);
    List<ReviewExport> findByReview(long userId, long reviewId);
    void deleteByReview(long userId, long reviewId);
    boolean deleteOwnedById(long userId, long exportId);
}
