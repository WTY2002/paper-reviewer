package com.paper.reviewer.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.database.entity.PaperEntity;
import com.paper.reviewer.database.entity.ReviewEntity;
import com.paper.reviewer.database.mapper.PaperMapper;
import com.paper.reviewer.database.mapper.ReviewMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private static final Set<String> TERMINAL = Set.of("COMPLETED", "FAILED", "DELETED");
    private final PaperMapper papers; private final ReviewMapper reviews;
    public DashboardService(PaperMapper papers, ReviewMapper reviews) { this.papers=papers; this.reviews=reviews; }
    public DashboardResponse get(long userId) {
        List<ReviewEntity> recentReviews = reviews.selectList(new LambdaQueryWrapper<ReviewEntity>().eq(ReviewEntity::getUserId,userId).orderByDesc(ReviewEntity::getCreatedAt).last("LIMIT 5"));
        List<Long> paperIds = recentReviews.stream().map(ReviewEntity::getPaperId).distinct().toList();
        Map<Long, PaperEntity> paperById = paperIds.isEmpty() ? Map.of() : papers.selectBatchIds(paperIds).stream()
                .filter(paper -> paper.getUserId().equals(userId))
                .collect(Collectors.toMap(PaperEntity::getId, Function.identity()));
        List<DashboardReviewItem> items = recentReviews.stream().map(review -> {
            PaperEntity paper = paperById.get(review.getPaperId());
            String title = paper == null ? "Untitled paper" : paper.getTitle();
            return new DashboardReviewItem(review.getId(), review.getPaperId(), title,
                    review.getReviewType(), review.getStatus(), review.getCreatedAt());
        }).toList();
        long paperCount = papers.selectCount(new LambdaQueryWrapper<PaperEntity>().eq(PaperEntity::getUserId, userId));
        long reviewCount = reviews.selectCount(new LambdaQueryWrapper<ReviewEntity>().eq(ReviewEntity::getUserId, userId));
        long activeCount = reviews.selectCount(new LambdaQueryWrapper<ReviewEntity>().eq(ReviewEntity::getUserId, userId)
                .notIn(ReviewEntity::getStatus, TERMINAL));
        long completedCount = reviews.selectCount(new LambdaQueryWrapper<ReviewEntity>().eq(ReviewEntity::getUserId, userId)
                .eq(ReviewEntity::getStatus, "COMPLETED"));
        return new DashboardResponse(paperCount, reviewCount, activeCount, completedCount, items);
    }
}
