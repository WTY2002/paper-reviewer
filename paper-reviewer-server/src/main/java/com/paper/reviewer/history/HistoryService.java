package com.paper.reviewer.history;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.export.domain.ReviewExport;
import com.paper.reviewer.export.repository.ExportRepository;
import com.paper.reviewer.paper.repository.PaperRepository;
import com.paper.reviewer.rereview.domain.ReReview;
import com.paper.reviewer.rereview.repository.ReReviewRepository;
import com.paper.reviewer.review.domain.Review;
import com.paper.reviewer.review.repository.ReviewRepository;
import com.paper.reviewer.storage.service.LocalFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Service
public class HistoryService {
    private final ReviewRepository reviews;
    private final ReReviewRepository rereviews;
    private final PaperRepository papers;
    private final ExportRepository exports;
    private final LocalFileStorageService storage;

    public HistoryService(ReviewRepository reviews, ReReviewRepository rereviews,
                          PaperRepository papers,
                          ExportRepository exports, LocalFileStorageService storage) {
        this.reviews = reviews;
        this.rereviews = rereviews;
        this.papers = papers;
        this.exports = exports;
        this.storage = storage;
    }

    @Transactional
    public void deleteReview(long userId, long reviewId) {
        Review review = reviews.findOwnedById(userId, reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        deleteRereviews(userId, reviewId);
        deleteExports(userId, reviewId);
        if (!reviews.deleteOwnedById(userId, reviewId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        long remaining = reviews.countOwnedByPaperId(userId, review.paperId());
        if (remaining == 0) {
            papers.deleteOwnedById(userId, review.paperId());
            storage.deleteDirectory(storage.paperDirectory(userId, review.paperId()));
        }
    }

    private void deleteRereviews(long userId, long reviewId) {
        List<ReReview> revisions = rereviews.findByOriginalReviewId(userId, reviewId);
        for (ReReview revision : revisions) {
            rereviews.deleteOwnedById(userId, revision.id());
            deletePaper(userId, revision.revisedPaperId());
            deletePaper(userId, revision.responsePaperId());
        }
    }

    private void deletePaper(long userId, long paperId) {
        papers.deleteOwnedById(userId, paperId);
        storage.deleteDirectory(storage.paperDirectory(userId, paperId));
    }

    private void deleteExports(long userId, long reviewId) {
        for (ReviewExport value : exports.findByReview(userId, reviewId)) {
            storage.deleteFile(Path.of(value.filePath()));
        }
        exports.deleteByReview(userId, reviewId);
        storage.deleteDirectory(storage.exportDirectory(userId, reviewId));
    }
}
