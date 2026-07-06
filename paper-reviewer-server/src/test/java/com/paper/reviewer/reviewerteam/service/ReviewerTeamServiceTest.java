package com.paper.reviewer.reviewerteam.service;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.reviewerteam.domain.Reviewer;
import com.paper.reviewer.reviewerteam.domain.ReviewerRole;
import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;
import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;
import com.paper.reviewer.reviewerteam.service.UpdateReviewerCommand;
import com.paper.reviewer.reviewerteam.service.UpdateReviewerTeamCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewerTeamServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-04T08:00:00Z"), ZoneOffset.UTC);
    private FakeRepository repository;
    private FakeReviewAccess reviews;
    private RecordingEvents events;
    private ReviewerTeamService service;

    @BeforeEach
    void setUp() {
        repository = new FakeRepository(team());
        reviews = new FakeReviewAccess("TEAM_PENDING");
        events = new RecordingEvents();
        service = new ReviewerTeamService(repository, reviews, events, clock);
    }

    @Test
    void editsOnlyVenueIdentityAndFocus() {
        List<UpdateReviewerCommand> edits = team().reviewers().stream()
                .map(r -> new UpdateReviewerCommand(r.role(), r.displayName(), "new " + r.role(),
                        r.expertise(), "focus " + r.role())).toList();

        ReviewerTeamService.TeamWithStatus result = service.edit(7L, 10L,
                new UpdateReviewerTeamCommand("New Venue", edits));

        assertThat(result.team().targetVenue()).isEqualTo("New Venue");
        assertThat(result.team().reviewers()).allSatisfy(r -> {
            assertThat(r.identityDescription()).startsWith("new ");
            assertThat(r.reviewFocus()).startsWith("focus ");
            assertThat(r.displayName()).isEqualTo(r.role() + " name");
            assertThat(r.expertise()).isEqualTo(r.role() + " expertise");
        });
    }

    @Test
    void rejectsChangingImmutableReviewerFields() {
        List<UpdateReviewerCommand> edits = requests();
        UpdateReviewerCommand eic = edits.get(0);
        edits.set(0, new UpdateReviewerCommand(eic.role(), "forged", eic.identityDescription(),
                eic.expertise(), eic.reviewFocus()));

        assertRequestRejected(new UpdateReviewerTeamCommand("Venue", edits), "displayName");

        edits = requests();
        eic = edits.get(0);
        edits.set(0, new UpdateReviewerCommand(eic.role(), eic.displayName(), eic.identityDescription(),
                "forged", eic.reviewFocus()));
        assertRequestRejected(new UpdateReviewerTeamCommand("Venue", edits), "expertise");
    }

    @Test
    void rejectsRoleChangesAndReviewerRemoval() {
        List<UpdateReviewerCommand> changed = requests();
        UpdateReviewerCommand first = changed.get(0);
        changed.set(0, new UpdateReviewerCommand(ReviewerRole.DOMAIN, first.displayName(),
                first.identityDescription(), first.expertise(), first.reviewFocus()));
        assertRequestRejected(new UpdateReviewerTeamCommand("Venue", changed), "roles");

        List<UpdateReviewerCommand> removed = requests().subList(0, 4);
        assertRequestRejected(new UpdateReviewerTeamCommand("Venue", removed), "five");
    }

    @Test
    void confirmationLocksTeamTransitionsReviewAndPublishesEvent() {
        ReviewerTeam confirmed = service.confirm(7L, 10L);

        assertThat(confirmed.confirmedAt()).isEqualTo(LocalDateTime.parse("2026-07-04T08:00:00"));
        assertThat(reviews.status).isEqualTo("TEAM_CONFIRMED");
        assertThat(events.confirmed).isEqualTo(1);
        assertThatThrownBy(() -> service.edit(7L, 10L,
                new UpdateReviewerTeamCommand("Other", requests())))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_INVALID_STATUS);
        assertThatThrownBy(() -> service.confirm(7L, 10L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cannotConfirmBeforeTeamPendingOrAccessAnotherUsersReview() {
        reviews.status = "ANALYZING";
        assertThatThrownBy(() -> service.confirm(7L, 10L)).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_INVALID_STATUS);

        assertThatThrownBy(() -> service.get(99L, 10L)).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    private void assertRequestRejected(UpdateReviewerTeamCommand request, String message) {
        assertThatThrownBy(() -> service.edit(7L, 10L, request))
                .isInstanceOf(BusinessException.class).hasMessageContaining(message)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REQUEST_VALIDATION_FAILED);
    }

    private List<UpdateReviewerCommand> requests() {
        return new ArrayList<>(team().reviewers().stream().map(r -> new UpdateReviewerCommand(r.role(),
                r.displayName(), r.identityDescription(), r.expertise(), r.reviewFocus())).toList());
    }

    private ReviewerTeam team() {
        List<Reviewer> reviewers = List.of(ReviewerRole.values()).stream()
                .map(role -> new Reviewer(role, role + " name", role + " identity",
                        role + " expertise", role + " focus")).toList();
        LocalDateTime created = LocalDateTime.parse("2026-07-04T07:00:00");
        return new ReviewerTeam(3L, 10L, "Venue", reviewers, null, created, created);
    }

    private static class FakeRepository implements ReviewerTeamRepository {
        private ReviewerTeam team;
        FakeRepository(ReviewerTeam team) { this.team = team; }
        public ReviewerTeam save(ReviewerTeam team) { this.team = team; return team; }
        public Optional<ReviewerTeam> findByReviewId(long reviewId) {
            return team != null && team.reviewId() == reviewId ? Optional.of(team) : Optional.empty();
        }
    }

    private static class FakeReviewAccess implements ReviewerTeamReviewAccess {
        private String status;
        FakeReviewAccess(String status) { this.status = status; }
        public Optional<ReviewState> findOwned(long reviewId, long userId) {
            return userId == 7 ? Optional.of(new ReviewState(reviewId, userId, status)) : Optional.empty();
        }
        public Optional<ReviewState> find(long reviewId) { return Optional.of(new ReviewState(reviewId, 7, status)); }
        public boolean transition(long reviewId, String expected, String next) {
            if (!status.equals(expected)) return false;
            status = next;
            return true;
        }
    }

    private static class RecordingEvents implements ReviewerTeamEventPublisher {
        int confirmed;
        public void generated(ReviewerTeam team) {}
        public void confirmed(ReviewerTeam team) { confirmed++; }
    }
}
