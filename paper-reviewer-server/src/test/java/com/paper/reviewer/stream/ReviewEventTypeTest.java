package com.paper.reviewer.stream;

import com.paper.reviewer.stream.domain.ReviewEventType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewEventTypeTest {
    @Test
    void includesAllContractEventTypes() {
        assertThat(Set.of(ReviewEventType.values())).contains(
                ReviewEventType.PAPER_UPLOADED,
                ReviewEventType.PDF_EXTRACTING,
                ReviewEventType.PDF_EXTRACTED,
                ReviewEventType.FIELD_ANALYSIS_STARTED,
                ReviewEventType.FIELD_ANALYSIS_COMPLETED,
                ReviewEventType.REVIEWER_TEAM_GENERATED,
                ReviewEventType.REVIEWER_TEAM_CONFIRMED,
                ReviewEventType.FULL_REVIEW_STARTED,
                ReviewEventType.QUICK_REVIEW_STARTED,
                ReviewEventType.REREVIEW_STARTED,
                ReviewEventType.REVIEWER_REPORT_STARTED,
                ReviewEventType.REVIEWER_REPORT_DELTA,
                ReviewEventType.REVIEWER_REPORT_COMPLETED,
                ReviewEventType.EDITORIAL_DECISION_STARTED,
                ReviewEventType.REVISION_ROADMAP_STARTED,
                ReviewEventType.AUTHOR_QUESTIONS_STARTED,
                ReviewEventType.REVIEW_COMPLETED,
                ReviewEventType.REVIEW_FAILED,
                ReviewEventType.EXPORT_COMPLETED);
    }
}
