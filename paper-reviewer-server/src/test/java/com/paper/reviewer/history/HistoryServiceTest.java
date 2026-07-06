package com.paper.reviewer.history;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.export.repository.ExportRepository;
import com.paper.reviewer.paper.repository.PaperRepository;
import com.paper.reviewer.rereview.repository.ReReviewRepository;
import com.paper.reviewer.review.domain.Review;
import com.paper.reviewer.review.domain.ReviewStatus;
import com.paper.reviewer.review.domain.ReviewType;
import com.paper.reviewer.review.repository.ReviewRepository;
import com.paper.reviewer.storage.service.LocalFileStorageService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HistoryServiceTest {
    private final ReviewRepository reviews = mock(ReviewRepository.class);
    private final ReReviewRepository rereviews = mock(ReReviewRepository.class);
    private final PaperRepository papers = mock(PaperRepository.class);
    private final ExportRepository exports = mock(ExportRepository.class);
    private final LocalFileStorageService storage = mock(LocalFileStorageService.class);
    private final HistoryService service = new HistoryService(reviews, rereviews, papers, exports, storage);

    @Test
    void rejectsDeletionOfAnotherUsersReview() {
        when(reviews.findOwnedById(2, 9)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteReview(2, 9)).isInstanceOf(BusinessException.class);
    }

    @Test
    void physicallyDeletesReviewAndUnreferencedPaper() {
        LocalDateTime now = LocalDateTime.now();
        Review review = new Review(9L, 1, 7, ReviewType.QUICK, ReviewStatus.COMPLETED,
                "en", "en", null, null, null, null, null, now, now);
        when(reviews.findOwnedById(1, 9)).thenReturn(Optional.of(review));
        when(reviews.deleteOwnedById(1, 9)).thenReturn(true);
        when(rereviews.findByOriginalReviewId(1, 9)).thenReturn(java.util.List.of());
        when(reviews.countOwnedByPaperId(1, 7)).thenReturn(0L);

        service.deleteReview(1, 9);

        verify(reviews).deleteOwnedById(1, 9);
        verify(papers).deleteOwnedById(1, 7);
    }
}
