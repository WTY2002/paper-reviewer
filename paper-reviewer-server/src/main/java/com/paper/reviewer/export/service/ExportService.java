package com.paper.reviewer.export.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.export.domain.*;
import com.paper.reviewer.export.repository.ExportRepository;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.service.PaperService;
import com.paper.reviewer.review.domain.*;
import com.paper.reviewer.review.repository.ReviewReportRepository;
import com.paper.reviewer.review.service.ReviewWorkflowService;
import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;
import com.paper.reviewer.rereview.domain.ReReview;
import com.paper.reviewer.rereview.service.ReReviewService;
import com.paper.reviewer.storage.service.LocalFileStorageService;
import com.paper.reviewer.stream.domain.ReviewEventType;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ExportService {
    private final ExportRepository exports; private final ReviewWorkflowService reviews;
    private final ReviewReportRepository reports; private final ReviewerTeamRepository teams;
    private final PaperService papers; private final ReReviewService rereviews;
    private final LocalFileStorageService storage; private final ReviewEventService events; private final Clock clock;
    public ExportService(ExportRepository exports,ReviewWorkflowService reviews,ReviewReportRepository reports,
            ReviewerTeamRepository teams,PaperService papers,ReReviewService rereviews,
            LocalFileStorageService storage,ReviewEventService events,Clock clock){
        this.exports=exports;this.reviews=reviews;this.reports=reports;this.teams=teams;this.papers=papers;
        this.rereviews=rereviews;this.storage=storage;this.events=events;this.clock=clock;}

    public ReviewExport exportReview(long userId,long reviewId,ExportType type){
        Review review=reviews.get(userId,reviewId);if(review.status()!=ReviewStatus.COMPLETED)throw new BusinessException(ErrorCode.REVIEW_INVALID_STATUS);
        Paper paper=papers.get(userId,review.paperId());String markdown=reviewMarkdown(paper,review);
        return persist(userId,reviewId,null,type,markdown,reviewId);
    }
    public ReviewExport exportRereview(long userId,long rereviewId,ExportType type){
        ReReview rereview=rereviews.get(userId,rereviewId);if(rereview.status()!=com.paper.reviewer.rereview.domain.ReReviewStatus.COMPLETED)throw new BusinessException(ErrorCode.REVIEW_INVALID_STATUS);
        String markdown="# Re-review Verification Report\n\n## Original Review\n\nReview ID: "+rereview.originalReviewId()+"\n\n"
                +"## Revised Manuscript\n\nPaper ID: "+rereview.revisedPaperId()+"\n\n## Author Response\n\nPaper ID: "+rereview.responsePaperId()+"\n\n"
                +"## Verification Checklist\n\n```json\n"+rereview.checklistJson().toPrettyString()+"\n```\n\n"
                +"## Residual and New Issues\n\n"+(rereview.resultMarkdown()==null?"":rereview.resultMarkdown());
        return persist(userId,null,rereviewId,type,markdown,rereview.originalReviewId());
    }
    public Resource download(long userId,long exportId){ReviewExport value=exports.findOwnedById(userId,exportId).orElseThrow(()->new BusinessException(ErrorCode.EXPORT_FAILED,"Export not found or access denied"));Path path=storage.readableExport(Path.of(value.filePath()));return new FileSystemResource(path);}
    public ReviewExport get(long userId,long exportId){return exports.findOwnedById(userId,exportId).orElseThrow(()->new BusinessException(ErrorCode.EXPORT_FAILED,"Export not found or access denied"));}

    String reviewMarkdown(Paper paper,Review review){
        StringBuilder out=new StringBuilder("# Academic Peer Review\n\n## Paper Information\n\n- **Title:** ").append(paper.title()).append("\n- **Review type:** ").append(review.reviewType()).append("\n\n");
        if(review.reviewType()==ReviewType.FULL){out.append("## Reviewer Team\n\n");teams.findByReviewId(review.id()).ifPresent(team->team.reviewers().forEach(r->out.append("- **").append(r.displayName()).append(":** ").append(r.identityDescription()).append("\n")));out.append("\n");}
        out.append(review.reviewType()==ReviewType.QUICK?"## EIC Quick Assessment\n\n":"## Independent Reviewer Reports\n\n");
        for(ReviewReport report:reports.findByReviewId(review.id()))out.append("### ").append(report.reviewerRole()).append("\n\n").append(nullToEmpty(report.contentMarkdown())).append("\n\n");
        if(review.reviewType()==ReviewType.FULL)out.append("## Editorial Decision\n\n").append(nullToEmpty(review.editorialDecisionMarkdown())).append("\n\n## Revision Roadmap\n\n").append(nullToEmpty(review.revisionRoadmapMarkdown())).append("\n\n## Questions for Authors\n\n").append(nullToEmpty(review.authorQuestionsMarkdown())).append("\n");
        else out.append("## Key Issues and Overall Recommendation\n\nSee the EIC assessment above.\n");
        return out.toString();
    }
    byte[] pdf(String markdown){try{String body=HtmlRenderer.builder().escapeHtml(true).build().render(Parser.builder().build().parse(markdown));String html="<html xmlns='http://www.w3.org/1999/xhtml'><head><meta charset='UTF-8' /><style>body{font-family:sans-serif;line-height:1.55;margin:36px;color:#17211b}h1,h2{color:#174b38}table{border-collapse:collapse;width:100%}td,th{border:1px solid #ccc;padding:6px}</style></head><body>"+body+"</body></html>";ByteArrayOutputStream output=new ByteArrayOutputStream();new PdfRendererBuilder().withHtmlContent(html,null).toStream(output).run();return output.toByteArray();}catch(Exception e){throw new BusinessException(ErrorCode.EXPORT_FAILED,"Could not render PDF",e);}}
    private ReviewExport persist(long userId,Long reviewId,Long rereviewId,ExportType type,String markdown,long directoryId){try{byte[] bytes=type==ExportType.MARKDOWN?markdown.getBytes(StandardCharsets.UTF_8):pdf(markdown);String name=(rereviewId==null?"review":"rereview")+(type==ExportType.MARKDOWN?".md":".pdf");Path path=storage.writeExport(userId,directoryId,name,bytes);LocalDateTime now=LocalDateTime.now(clock);ReviewExport saved=exports.save(new ReviewExport(null,userId,reviewId,rereviewId,type,path.toString(),ExportStatus.COMPLETED,now,now));events.publish(directoryId,ReviewEventType.EXPORT_COMPLETED,"EXPORT",null,null);return saved;}catch(BusinessException e){throw e;}catch(RuntimeException e){throw new BusinessException(ErrorCode.EXPORT_FAILED,"Export failed",e);}}
    private String nullToEmpty(String value){return value==null?"":value;}
}
