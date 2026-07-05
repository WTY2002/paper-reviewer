package com.paper.reviewer.dashboard;

import java.time.LocalDateTime;

public record DashboardReviewItem(long reviewId, long paperId, String paperTitle,
                                  String reviewType, String status, LocalDateTime createdAt) { }
