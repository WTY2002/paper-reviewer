package com.paper.reviewer.history;
import com.paper.reviewer.paper.domain.Paper;import com.paper.reviewer.review.domain.Review;import com.paper.reviewer.rereview.domain.ReReview;import java.util.List;
public record HistoryPaperDetail(Paper paper,List<Review> reviews,List<ReReview> rereviews){}
