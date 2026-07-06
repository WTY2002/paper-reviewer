package com.paper.reviewer.rereview.controller;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.common.ApiResponse;
import com.paper.reviewer.rereview.dto.ReReviewResponse;
import com.paper.reviewer.rereview.service.ReReviewService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ReReviewController {
    private final ReReviewService service;
    public ReReviewController(ReReviewService service){this.service=service;}
    @PostMapping(value="/api/reviews/{reviewId}/rereviews", consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ReReviewResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable long reviewId, @RequestPart MultipartFile revisedFile,
            @RequestPart MultipartFile responseFile,
            @RequestPart(value="outputLanguage", required=false) String outputLanguage) {
        return ApiResponse.success(ReReviewResponse.from(service.create(user.userId(),reviewId,revisedFile,responseFile,outputLanguage)));
    }
    @PostMapping("/api/rereviews/{rereviewId}/start")
    public ApiResponse<ReReviewResponse> start(@AuthenticationPrincipal AuthenticatedUser user,@PathVariable long rereviewId){return ApiResponse.success(ReReviewResponse.from(service.start(user.userId(),rereviewId)));}
    @GetMapping("/api/rereviews/{rereviewId}")
    public ApiResponse<ReReviewResponse> get(@AuthenticationPrincipal AuthenticatedUser user,@PathVariable long rereviewId){return ApiResponse.success(ReReviewResponse.from(service.get(user.userId(),rereviewId)));}
}
