package com.paper.reviewer.history;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.database.entity.RereviewEntity;
import com.paper.reviewer.database.entity.ReviewEntity;
import com.paper.reviewer.database.mapper.PaperMapper;
import com.paper.reviewer.database.mapper.RereviewMapper;
import com.paper.reviewer.database.mapper.ReviewMapper;
import com.paper.reviewer.export.domain.ReviewExport;
import com.paper.reviewer.export.repository.ExportRepository;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.repository.PaperRepository;
import com.paper.reviewer.review.domain.Review;
import com.paper.reviewer.review.repository.ReviewRepository;
import com.paper.reviewer.rereview.domain.ReReview;
import com.paper.reviewer.rereview.repository.ReReviewRepository;
import com.paper.reviewer.storage.service.LocalFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Service
public class HistoryService {
    private final PaperRepository papers;private final ReviewRepository reviews;private final ReReviewRepository rereviews;
    private final ReviewMapper reviewMapper;private final RereviewMapper rereviewMapper;private final PaperMapper paperMapper;
    private final ExportRepository exports;private final LocalFileStorageService storage;
    public HistoryService(PaperRepository papers,ReviewRepository reviews,ReReviewRepository rereviews,
            ReviewMapper reviewMapper,RereviewMapper rereviewMapper,PaperMapper paperMapper,
            ExportRepository exports,LocalFileStorageService storage){this.papers=papers;this.reviews=reviews;this.rereviews=rereviews;this.reviewMapper=reviewMapper;this.rereviewMapper=rereviewMapper;this.paperMapper=paperMapper;this.exports=exports;this.storage=storage;}
    public List<Paper> papers(long userId){return papers.findAllOwnedBy(userId);}
    public List<Review> reviews(long userId){return reviews.findAllOwnedBy(userId);}
    public HistoryPaperDetail detail(long userId,long paperId){Paper paper=ownedPaper(userId,paperId);List<Review> related=reviews.findAllOwnedBy(userId).stream().filter(r->r.paperId()==paperId).toList();List<Long> ids=related.stream().map(Review::id).toList();List<ReReview> re=ids.stream().flatMap(id->rereviewMapper.selectList(new LambdaQueryWrapper<RereviewEntity>().eq(RereviewEntity::getUserId,userId).eq(RereviewEntity::getOriginalReviewId,id)).stream()).map(e->rereviews.findOwnedById(userId,e.getId()).orElse(null)).filter(java.util.Objects::nonNull).toList();return new HistoryPaperDetail(paper,related,re);}
    @Transactional public void deleteReview(long userId,long reviewId){Review review=reviews.findOwnedById(userId,reviewId).orElseThrow(()->new BusinessException(ErrorCode.REVIEW_NOT_FOUND));List<RereviewEntity> revisions=rereviewMapper.selectList(new LambdaQueryWrapper<RereviewEntity>().eq(RereviewEntity::getUserId,userId).eq(RereviewEntity::getOriginalReviewId,reviewId));for(RereviewEntity re:revisions){rereviewMapper.hardDeleteOwned(userId,re.getId());paperMapper.hardDeleteOwned(userId,re.getRevisedPaperId());paperMapper.hardDeleteOwned(userId,re.getResponsePaperId());storage.deleteDirectory(storage.paperDirectory(userId,re.getRevisedPaperId()));storage.deleteDirectory(storage.paperDirectory(userId,re.getResponsePaperId()));}deleteExports(userId,review.id());if(!reviews.deleteOwnedById(userId,review.id()))throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);long remaining=reviewMapper.selectCount(new LambdaQueryWrapper<ReviewEntity>().eq(ReviewEntity::getUserId,userId).eq(ReviewEntity::getPaperId,review.paperId()));if(remaining==0){paperMapper.hardDeleteOwned(userId,review.paperId());storage.deleteDirectory(storage.paperDirectory(userId,review.paperId()));}}
    @Transactional public void deletePaper(long userId,long paperId){Paper paper=ownedPaper(userId,paperId);List<ReviewEntity> related=reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>().eq(ReviewEntity::getUserId,userId).eq(ReviewEntity::getPaperId,paperId));for(ReviewEntity review:related){List<RereviewEntity> revisions=rereviewMapper.selectList(new LambdaQueryWrapper<RereviewEntity>().eq(RereviewEntity::getUserId,userId).eq(RereviewEntity::getOriginalReviewId,review.getId()));for(RereviewEntity re:revisions){rereviewMapper.hardDeleteOwned(userId,re.getId());paperMapper.hardDeleteOwned(userId,re.getRevisedPaperId());paperMapper.hardDeleteOwned(userId,re.getResponsePaperId());storage.deleteDirectory(storage.paperDirectory(userId,re.getRevisedPaperId()));storage.deleteDirectory(storage.paperDirectory(userId,re.getResponsePaperId()));}deleteExports(userId,review.getId());reviewMapper.hardDeleteOwned(userId,review.getId());}if(!papers.deleteOwnedById(userId,paperId))throw new BusinessException(ErrorCode.PAPER_NOT_FOUND);storage.deleteDirectory(storage.paperDirectory(userId,paper.id()));}
    private void deleteExports(long userId,long reviewId){for(ReviewExport value:exports.findByReview(userId,reviewId))storage.deleteFile(Path.of(value.filePath()));exports.deleteByReview(userId,reviewId);storage.deleteDirectory(storage.exportDirectory(userId,reviewId));}
    private Paper ownedPaper(long userId,long id){return papers.findOwnedById(userId,id).orElseThrow(()->new BusinessException(ErrorCode.PAPER_NOT_FOUND));}
}
