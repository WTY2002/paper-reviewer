package com.paper.reviewer.paper.infrastructure.persistence;

import com.paper.reviewer.paper.repository.PaperRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.paper.infrastructure.persistence.PaperEntity;
import com.paper.reviewer.paper.infrastructure.persistence.PaperMapper;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.storage.service.PaperStorageUsageReader;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisPaperRepository implements PaperRepository, PaperStorageUsageReader {
    private final PaperMapper mapper;

    public MyBatisPaperRepository(PaperMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Paper save(Paper paper) {
        LocalDateTime now = LocalDateTime.now();
        PaperEntity entity = new PaperEntity();
        entity.setUserId(paper.userId());
        entity.setTitle(paper.title());
        entity.setOriginalFilename(paper.originalFilename());
        entity.setFilePath(paper.filePath());
        entity.setFileSize(paper.fileSize());
        entity.setPageCount(paper.pageCount());
        entity.setLanguage(paper.language());
        entity.setStatus(paper.status());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Paper update(Paper paper) {
        PaperEntity entity = mapper.selectById(paper.id());
        if (entity == null || !entity.getUserId().equals(paper.userId())) return paper;
        entity.setTitle(paper.title());
        entity.setFilePath(paper.filePath());
        entity.setPageCount(paper.pageCount());
        entity.setLanguage(paper.language());
        entity.setStatus(paper.status());
        entity.setUpdatedAt(LocalDateTime.now());
        mapper.updateById(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Paper> findOwnedById(long userId, long paperId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<PaperEntity>()
                .eq(PaperEntity::getId, paperId).eq(PaperEntity::getUserId, userId)))
                .map(this::toDomain);
    }

    @Override
    public List<Paper> findAllOwnedBy(long userId) {
        return mapper.selectList(new LambdaQueryWrapper<PaperEntity>()
                        .eq(PaperEntity::getUserId, userId).orderByDesc(PaperEntity::getCreatedAt))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long sumActiveFileSize(long userId) {
        return mapper.sumActiveFileSize(userId);
    }

    @Override
    public long getActivePaperFileSize(long userId) {
        return sumActiveFileSize(userId);
    }

    @Override
    public boolean deleteOwnedById(long userId, long paperId) {
        return mapper.hardDeleteOwned(userId, paperId) == 1;
    }

    private Paper toDomain(PaperEntity entity) {
        return new Paper(entity.getId(), entity.getUserId(), entity.getTitle(), entity.getOriginalFilename(),
                entity.getFilePath(), entity.getFileSize(), entity.getPageCount(), entity.getLanguage(),
                entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
