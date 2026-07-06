package com.paper.reviewer.dashboard;
import com.paper.reviewer.paper.infrastructure.persistence.PaperEntity;
import com.paper.reviewer.review.infrastructure.persistence.ReviewEntity;
import com.paper.reviewer.paper.infrastructure.persistence.PaperMapper;
import com.paper.reviewer.review.infrastructure.persistence.ReviewMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
class DashboardServiceTest {
    @Test void returnsCountsAndRecentReviewsWithPaperTitles(){
        PaperMapper papers=mock(PaperMapper.class);ReviewMapper reviews=mock(ReviewMapper.class);
        PaperEntity paper=new PaperEntity();paper.setId(3L);paper.setUserId(9L);paper.setTitle("Careful Science");
        ReviewEntity active=new ReviewEntity();active.setId(5L);active.setPaperId(3L);active.setReviewType("FULL");active.setStatus("REVIEWING");
        when(reviews.selectList(any())).thenReturn(List.of(active));when(papers.selectBatchIds(any())).thenReturn(List.of(paper));
        when(papers.selectCount(any())).thenReturn(2L);when(reviews.selectCount(any())).thenReturn(4L,1L,3L);
        DashboardResponse result=new DashboardService(papers,reviews).get(9L);
        assertThat(result.paperCount()).isEqualTo(2);assertThat(result.reviewCount()).isEqualTo(4);
        assertThat(result.activeReviewCount()).isEqualTo(1);assertThat(result.completedReviewCount()).isEqualTo(3);
        assertThat(result.recentReviews()).singleElement().satisfies(item -> {
            assertThat(item.paperTitle()).isEqualTo("Careful Science");assertThat(item.reviewId()).isEqualTo(5);
        });
    }
}
