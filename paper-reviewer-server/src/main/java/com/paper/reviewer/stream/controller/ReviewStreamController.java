package com.paper.reviewer.stream.controller;

import com.paper.reviewer.auth.security.AuthenticatedUser;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.stream.service.ReviewEventService;
import com.paper.reviewer.stream.service.ReviewOwnershipReader;
import com.paper.reviewer.stream.service.SseConnectionManager;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/reviews")
public class ReviewStreamController {
    private final ReviewOwnershipReader ownershipReader;
    private final ReviewEventService eventService;
    private final SseConnectionManager connectionManager;

    public ReviewStreamController(ReviewOwnershipReader ownershipReader, ReviewEventService eventService,
                                  SseConnectionManager connectionManager) {
        this.ownershipReader = ownershipReader;
        this.eventService = eventService;
        this.connectionManager = connectionManager;
    }

    @GetMapping(value = "/{reviewId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal AuthenticatedUser user,
                             @PathVariable long reviewId) {
        if (!ownershipReader.isOwnedBy(reviewId, user.userId())) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return connectionManager.subscribe(reviewId, () -> eventService.history(reviewId));
    }
}
