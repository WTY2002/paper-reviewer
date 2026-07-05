package com.paper.reviewer.paper.repository;

import com.paper.reviewer.paper.domain.Paper;

import java.util.List;
import java.util.Optional;

public interface PaperRepository {
    Paper save(Paper paper);
    Paper update(Paper paper);
    Optional<Paper> findOwnedById(long userId, long paperId);
    List<Paper> findAllOwnedBy(long userId);
    long sumActiveFileSize(long userId);
    boolean deleteOwnedById(long userId, long paperId);
}
