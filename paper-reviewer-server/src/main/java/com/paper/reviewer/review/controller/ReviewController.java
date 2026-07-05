package com.paper.reviewer.review.controller;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.common.ApiResponse;
import com.paper.reviewer.review.domain.Review;
import com.paper.reviewer.review.dto.CreateReviewRequest;
import com.paper.reviewer.review.dto.ReviewDetailResponse;
import com.paper.reviewer.review.dto.ReviewSummaryResponse;
import com.paper.reviewer.review.service.ReviewWorkflowService;
import com.paper.reviewer.history.HistoryService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewWorkflowService workflow;
    private final HistoryService history;
    public ReviewController(ReviewWorkflowService workflow,HistoryService history) { this.workflow = workflow;this.history=history; }

    @PostMapping("/analysis")
    public ApiResponse<ReviewSummaryResponse> analyze(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @Valid @RequestBody CreateReviewRequest request) {
        Review review = workflow.createAndAnalyze(user.userId(), request.paperId(), request.reviewType(),
                request.outputLanguage());
        return ApiResponse.success(ReviewSummaryResponse.from(review));
    }

    @PostMapping("/{reviewId}/start")
    public ApiResponse<ReviewSummaryResponse> start(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable long reviewId) {
        return ApiResponse.success(ReviewSummaryResponse.from(workflow.start(user.userId(), reviewId)));
    }

    @GetMapping
    public ApiResponse<List<ReviewSummaryResponse>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(workflow.list(user.userId()).stream()
                .map(review -> ReviewSummaryResponse.from(review,
                        workflow.paperTitle(user.userId(), review.paperId())))
                .toList());
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDetailResponse> detail(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable long reviewId) {
        Review review = workflow.get(user.userId(), reviewId);
        return ApiResponse.success(ReviewDetailResponse.from(review,
                workflow.paperTitle(user.userId(), review.paperId()),
                workflow.getReports(user.userId(), reviewId)));
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable long reviewId) {
        history.deleteReview(user.userId(), reviewId);
        return ApiResponse.success(null);
    }
}
