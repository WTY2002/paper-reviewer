package com.paper.reviewer.rereview.service;

import com.paper.reviewer.ai.orchestrator.ReReviewOrchestrator;
import com.paper.reviewer.ai.parser.ReReviewChecklist;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.extraction.domain.PaperExtraction;
import com.paper.reviewer.extraction.repository.PaperExtractionRepository;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.service.PaperService;
import com.paper.reviewer.review.domain.*;
import com.paper.reviewer.review.repository.ReviewReportRepository;
import com.paper.reviewer.review.service.ReviewWorkflowService;
import com.paper.reviewer.rereview.domain.*;
import com.paper.reviewer.rereview.repository.ReReviewRepository;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.time.*;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReReviewServiceTest {
    private final ReReviewRepository repo=mock(ReReviewRepository.class);private final ReviewWorkflowService reviews=mock(ReviewWorkflowService.class);
    private final ReviewReportRepository reports=mock(ReviewReportRepository.class);private final PaperService papers=mock(PaperService.class);
    private final PaperExtractionRepository extractions=mock(PaperExtractionRepository.class);private final ReReviewOrchestrator ai=mock(ReReviewOrchestrator.class);
    private final ReviewEventService events=mock(ReviewEventService.class);private final Clock clock=Clock.fixed(Instant.parse("2026-07-04T00:00:00Z"),ZoneOffset.UTC);
    private final ReReviewService service=new ReReviewService(repo,reviews,reports,papers,extractions,ai,events,new ObjectMapper(),clock);

    @Test void refusesIncompleteOriginalReview(){when(reviews.get(1,2)).thenReturn(review(ReviewStatus.REVIEWING));assertThatThrownBy(()->service.create(1,2,pdf("r.pdf"),pdf("a.pdf"),"AUTO")).isInstanceOf(BusinessException.class);verifyNoInteractions(papers);}
    @Test void createsAndCompletesChecklist(){
        when(reviews.get(1,2)).thenReturn(review(ReviewStatus.COMPLETED));when(papers.upload(eq(1L),any())).thenReturn(paper(10),paper(11));
        when(repo.save(any())).thenAnswer(i->withId(i.getArgument(0),20));when(repo.findOwnedById(1,20)).thenReturn(Optional.of(rereview()));
        when(repo.update(any())).thenAnswer(i->i.getArgument(0));when(extractions.findByPaperId(anyLong())).thenReturn(Optional.of(extraction()));
        when(ai.review(anyLong(),anyString(),anyString(),anyString(),anyString())).thenReturn(new ReReviewChecklist("ACCEPT",List.of(),List.of(),List.of(),"# Verified"));
        ReReview made=service.create(1,2,pdf("r.pdf"),pdf("a.pdf"),"en");assertThat(made.id()).isEqualTo(20);
        ReReview done=service.start(1,20);assertThat(done.status()).isEqualTo(ReReviewStatus.COMPLETED);assertThat(done.checklistJson().get("decision").asString()).isEqualTo("ACCEPT");
    }
    @Test void rejectsOtherUsersRereview(){when(repo.findOwnedById(9,20)).thenReturn(Optional.empty());assertThatThrownBy(()->service.get(9,20)).isInstanceOf(BusinessException.class);}
    private MockMultipartFile pdf(String n){return new MockMultipartFile("file",n,"application/pdf","%PDF-1.4".getBytes());}
    private Review review(ReviewStatus s){return new Review(2L,1,3,ReviewType.FULL,s,"en","en",null,null,"roadmap",null,null,LocalDateTime.now(),LocalDateTime.now());}
    private Paper paper(long id){return new Paper(id,1L,"p","p.pdf","x",5,1,"en","EXTRACTED",LocalDateTime.now(),LocalDateTime.now());}
    private PaperExtraction extraction(){return new PaperExtraction(1L,10L,"text",1,"COMPLETED",null,LocalDateTime.now(),LocalDateTime.now());}
    private ReReview rereview(){return new ReReview(20L,1,2,10,11,"en",ReReviewStatus.CREATED,null,null,null,LocalDateTime.now(),LocalDateTime.now());}
    private ReReview withId(ReReview r,long id){return new ReReview(id,r.userId(),r.originalReviewId(),r.revisedPaperId(),r.responsePaperId(),r.outputLanguage(),r.status(),r.resultMarkdown(),r.checklistJson(),r.errorMessage(),r.createdAt(),r.updatedAt());}
}
