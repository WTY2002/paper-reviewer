package com.paper.reviewer.reviewerteam.service;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.reviewerteam.domain.Reviewer;
import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;
import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;
import com.paper.reviewer.reviewerteam.web.UpdateReviewerRequest;
import com.paper.reviewer.reviewerteam.web.UpdateReviewerTeamRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewerTeamService {
    public static final String TEAM_PENDING = "TEAM_PENDING";
    public static final String TEAM_CONFIRMED = "TEAM_CONFIRMED";
    private static final Set<String> GENERATION_STATUSES = Set.of("ANALYZING", "FIELD_ANALYSIS_COMPLETED");

    private final ReviewerTeamRepository teams;
    private final ReviewerTeamReviewAccess reviews;
    private final ReviewerTeamEventPublisher events;
    private final Clock clock;

    public ReviewerTeamService(ReviewerTeamRepository teams, ReviewerTeamReviewAccess reviews,
                               ReviewerTeamEventPublisher events, Clock clock) {
        this.teams = teams;
        this.reviews = reviews;
        this.events = events;
        this.clock = clock;
    }

    /** Integration point for the AI orchestration after field analysis. */
    @Transactional
    public ReviewerTeam saveGeneratedTeam(long reviewId, String targetVenue, List<Reviewer> reviewers) {
        ReviewerTeamReviewAccess.ReviewState review = reviews.find(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        if (!GENERATION_STATUSES.contains(review.status()) || teams.findByReviewId(reviewId).isPresent()) {
            throw invalidStatus("Reviewer team cannot be generated in status " + review.status());
        }
        LocalDateTime now = LocalDateTime.now(clock);
        ReviewerTeam saved;
        try {
            saved = teams.save(new ReviewerTeam(null, reviewId, targetVenue, reviewers, null, now, now));
        } catch (IllegalArgumentException exception) {
            throw invalidRequest(exception.getMessage());
        }
        if (!reviews.transition(reviewId, review.status(), TEAM_PENDING)) {
            throw invalidStatus("Review status changed while generating reviewer team");
        }
        events.generated(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public TeamWithStatus get(long userId, long reviewId) {
        ReviewerTeamReviewAccess.ReviewState review = owned(userId, reviewId);
        return new TeamWithStatus(team(reviewId), review.status());
    }

    @Transactional
    public TeamWithStatus edit(long userId, long reviewId, UpdateReviewerTeamRequest request) {
        ReviewerTeamReviewAccess.ReviewState review = owned(userId, reviewId);
        requireStatus(review, TEAM_PENDING);
        ReviewerTeam current = team(reviewId);
        try {
            validateRequestedRoles(request.reviewers());
            List<Reviewer> edited = request.reviewers().stream().map(candidate -> merge(current, candidate)).toList();
            ReviewerTeam saved = teams.save(current.edit(request.targetVenue(), edited, LocalDateTime.now(clock)));
            return new TeamWithStatus(saved, review.status());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw invalidRequest(exception.getMessage());
        }
    }

    @Transactional
    public ReviewerTeam confirm(long userId, long reviewId) {
        ReviewerTeamReviewAccess.ReviewState review = owned(userId, reviewId);
        requireStatus(review, TEAM_PENDING);
        ReviewerTeam confirmed;
        try {
            confirmed = teams.save(team(reviewId).confirm(LocalDateTime.now(clock)));
        } catch (IllegalStateException exception) {
            throw invalidStatus(exception.getMessage());
        }
        if (!reviews.transition(reviewId, TEAM_PENDING, TEAM_CONFIRMED)) {
            throw invalidStatus("Review status changed while confirming reviewer team");
        }
        events.confirmed(confirmed);
        return confirmed;
    }

    private Reviewer merge(ReviewerTeam current, UpdateReviewerRequest candidate) {
        Reviewer existing = current.reviewers().stream()
                .filter(reviewer -> reviewer.role() == candidate.role()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reviewer roles cannot be changed"));
        if (candidate.displayName() != null && !candidate.displayName().equals(existing.displayName())) {
            throw new IllegalArgumentException("displayName cannot be changed");
        }
        if (candidate.expertise() != null && !candidate.expertise().equals(existing.expertise())) {
            throw new IllegalArgumentException("expertise cannot be changed");
        }
        return new Reviewer(existing.role(), existing.displayName(), candidate.identityDescription(),
                existing.expertise(), candidate.reviewFocus());
    }

    private void validateRequestedRoles(List<UpdateReviewerRequest> requested) {
        if (requested.size() != com.paper.reviewer.reviewerteam.domain.ReviewerRole.values().length) {
            throw new IllegalArgumentException("Reviewer team must contain exactly five reviewers");
        }
        Set<com.paper.reviewer.reviewerteam.domain.ReviewerRole> roles = requested.stream()
                .map(UpdateReviewerRequest::role).collect(Collectors.toSet());
        if (roles.size() != requested.size()
                || roles.size() != com.paper.reviewer.reviewerteam.domain.ReviewerRole.values().length) {
            throw new IllegalArgumentException("Reviewer roles cannot be changed");
        }
    }

    private ReviewerTeamReviewAccess.ReviewState owned(long userId, long reviewId) {
        return reviews.findOwned(reviewId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private ReviewerTeam team(long reviewId) {
        return teams.findByReviewId(reviewId)
                .orElseThrow(() -> invalidStatus("Reviewer team has not been generated"));
    }

    private void requireStatus(ReviewerTeamReviewAccess.ReviewState review, String status) {
        if (!status.equals(review.status())) throw invalidStatus("Review status does not allow team changes");
    }

    private BusinessException invalidStatus(String message) {
        return new BusinessException(ErrorCode.REVIEW_INVALID_STATUS, message);
    }

    private BusinessException invalidRequest(String message) {
        return new BusinessException(ErrorCode.REQUEST_VALIDATION_FAILED, message);
    }

    public record TeamWithStatus(ReviewerTeam team, String status) {}
}
