package com.paper.reviewer.export.infrastructure.persistence;

import com.paper.reviewer.export.repository.ExportRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.export.infrastructure.persistence.ExportEntity;
import com.paper.reviewer.export.infrastructure.persistence.ExportMapper;
import com.paper.reviewer.export.domain.ExportStatus;
import com.paper.reviewer.export.domain.ExportType;
import com.paper.reviewer.export.domain.ReviewExport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisExportRepository implements ExportRepository {
    private final ExportMapper mapper;
    public MyBatisExportRepository(ExportMapper mapper) { this.mapper = mapper; }

    @Override public ReviewExport save(ReviewExport value) {
        ExportEntity e = toEntity(value);
        if (value.id() == null) mapper.insert(e); else mapper.updateById(e);
        return toDomain(e);
    }
    @Override public Optional<ReviewExport> findOwnedById(long userId, long exportId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ExportEntity>()
                .eq(ExportEntity::getId, exportId).eq(ExportEntity::getUserId, userId))).map(this::toDomain);
    }
    @Override public List<ReviewExport> findByReview(long userId, long reviewId) {
        return mapper.selectList(new LambdaQueryWrapper<ExportEntity>().eq(ExportEntity::getUserId, userId)
                .eq(ExportEntity::getReviewId, reviewId)).stream().map(this::toDomain).toList();
    }
    @Override public void deleteByReview(long userId, long reviewId) {
        mapper.hardDeleteByReview(userId, reviewId);
    }
    private ExportEntity toEntity(ReviewExport v) {
        ExportEntity e = new ExportEntity(); e.setId(v.id()); e.setUserId(v.userId());
        e.setReviewId(v.reviewId()); e.setRereviewId(v.rereviewId()); e.setExportType(v.exportType().name());
        e.setFilePath(v.filePath()); e.setStatus(v.status().name()); e.setCreatedAt(v.createdAt()); e.setUpdatedAt(v.updatedAt());
        return e;
    }
    private ReviewExport toDomain(ExportEntity e) {
        return new ReviewExport(e.getId(), e.getUserId(), e.getReviewId(), e.getRereviewId(),
                ExportType.valueOf(e.getExportType()), e.getFilePath(), ExportStatus.valueOf(e.getStatus()),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
