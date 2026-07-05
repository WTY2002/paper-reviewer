package com.paper.reviewer.dashboard;
import java.util.List;
public record DashboardResponse(long paperCount, long reviewCount, long activeReviewCount,
                                long completedReviewCount, List<DashboardReviewItem> recentReviews) { }
