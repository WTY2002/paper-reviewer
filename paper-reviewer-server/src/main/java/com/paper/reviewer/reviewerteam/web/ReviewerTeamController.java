package com.paper.reviewer.reviewerteam.web;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.common.ApiResponse;
import com.paper.reviewer.reviewerteam.service.ReviewerTeamService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews/{reviewId}")
public class ReviewerTeamController {
    private final ReviewerTeamService service;

    public ReviewerTeamController(ReviewerTeamService service) { this.service = service; }

    @GetMapping("/team")
    public ApiResponse<ReviewerTeamResponse> get(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @PathVariable long reviewId) {
        ReviewerTeamService.TeamWithStatus result = service.get(user.userId(), reviewId);
        return ApiResponse.success(ReviewerTeamResponse.from(result.team(), result.status()));
    }

    @PutMapping("/team")
    public ApiResponse<ReviewerTeamResponse> edit(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @PathVariable long reviewId,
                                                  @Valid @RequestBody UpdateReviewerTeamRequest request) {
        ReviewerTeamService.TeamWithStatus result = service.edit(user.userId(), reviewId, request);
        return ApiResponse.success(ReviewerTeamResponse.from(result.team(), result.status()));
    }

    @PostMapping("/confirm-team")
    public ApiResponse<ConfirmTeamResponse> confirm(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable long reviewId) {
        service.confirm(user.userId(), reviewId);
        return ApiResponse.success(new ConfirmTeamResponse(reviewId, ReviewerTeamService.TEAM_CONFIRMED));
    }
}
