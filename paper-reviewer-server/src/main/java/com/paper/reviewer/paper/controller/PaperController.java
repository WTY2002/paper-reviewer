package com.paper.reviewer.paper.controller;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.common.ApiResponse;
import com.paper.reviewer.paper.dto.PaperResponse;
import com.paper.reviewer.paper.service.PaperService;
import com.paper.reviewer.history.HistoryService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/papers")
public class PaperController {
    private final PaperService paperService;
    private final HistoryService historyService;

    public PaperController(PaperService paperService, HistoryService historyService) { this.paperService = paperService; this.historyService=historyService; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PaperResponse> upload(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestPart("file") MultipartFile file) {
        return ApiResponse.success(PaperResponse.from(paperService.upload(user.userId(), file)));
    }

    @GetMapping
    public ApiResponse<List<PaperResponse>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(paperService.list(user.userId()).stream().map(PaperResponse::from).toList());
    }

    @GetMapping("/{paperId}")
    public ApiResponse<PaperResponse> detail(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable long paperId) {
        return ApiResponse.success(PaperResponse.from(paperService.get(user.userId(), paperId)));
    }

    @GetMapping(value = "/{paperId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> pdf(@AuthenticationPrincipal AuthenticatedUser user,
                                        @PathVariable long paperId) throws IOException {
        Resource resource = paperService.getPdf(user.userId(), paperId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=original.pdf")
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @DeleteMapping("/{paperId}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable long paperId) {
        historyService.deletePaper(user.userId(), paperId);
        return ApiResponse.success(null);
    }
}
