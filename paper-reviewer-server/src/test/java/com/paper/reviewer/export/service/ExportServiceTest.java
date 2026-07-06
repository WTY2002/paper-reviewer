package com.paper.reviewer.export.service;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.export.repository.ExportRepository;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.review.domain.*;
import com.paper.reviewer.review.repository.ReviewReportRepository;
import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;
import org.junit.jupiter.api.Test;
import java.time.*;import java.util.*;
import static org.assertj.core.api.Assertions.*;import static org.mockito.Mockito.*;

class ExportServiceTest {
    private final ExportRepository exports=mock(ExportRepository.class);private final ReviewReportRepository reports=mock(ReviewReportRepository.class);private final ReviewerTeamRepository teams=mock(ReviewerTeamRepository.class);
    private final ExportService service=new ExportService(exports,null,reports,teams,null,null,null,null,Clock.systemUTC());
    @Test void fullMarkdownContainsRequiredSections(){Review review=review(ReviewType.FULL);when(reports.findByReviewId(2)).thenReturn(List.of(report("EIC"),report("METHODOLOGY"),report("DOMAIN"),report("PERSPECTIVE"),report("DEVILS_ADVOCATE")));when(teams.findByReviewId(2)).thenReturn(Optional.empty());String markdown=service.reviewMarkdown(paper(),review);assertThat(markdown).contains("Paper Information","Reviewer Team","Independent Reviewer Reports","Editorial Decision","Revision Roadmap","Questions for Authors","DEVILS_ADVOCATE");}
    @Test void quickMarkdownContainsEicAndRecommendation(){when(reports.findByReviewId(2)).thenReturn(List.of(report("EIC")));assertThat(service.reviewMarkdown(paper(),review(ReviewType.QUICK))).contains("EIC Quick Assessment","Key Issues and Overall Recommendation");}
    @Test void pdfIsNonEmpty(){assertThat(service.pdf("# Review\n\nConstructive result.")).hasSizeGreaterThan(500);}
    @Test void downloadRejectsOtherUsers(){when(exports.findOwnedById(9,44)).thenReturn(Optional.empty());assertThatThrownBy(()->service.download(9,44)).isInstanceOf(BusinessException.class);}
    private Paper paper(){return new Paper(1L,1L,"Research","paper.pdf","x",10,2,"en","EXTRACTED",LocalDateTime.now(),LocalDateTime.now());}
    private Review review(ReviewType type){return new Review(2L,1,1,type,ReviewStatus.COMPLETED,"en","en",null,"Decision","Roadmap","Questions",null,LocalDateTime.now(),LocalDateTime.now());}
    private ReviewReport report(String role){return new ReviewReport(null,2,role,"Report "+role,null,"COMPLETED",LocalDateTime.now(),LocalDateTime.now());}
}
