package com.paper.reviewer.common;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements, int pageNumber, int pageSize,
                            int totalPages) {

    public PageResult {
        content = List.copyOf(content);
        if (totalElements < 0 || pageNumber < 0 || pageSize < 1 || totalPages < 0) {
            throw new IllegalArgumentException("Invalid pagination values");
        }
    }

    public static <T> PageResult<T> of(List<T> content, long totalElements, int pageNumber,
                                       int pageSize) {
        long pages = totalElements / pageSize + (totalElements % pageSize == 0 ? 0 : 1);
        if (pages > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total page count exceeds supported range");
        }
        int totalPages = (int) pages;
        return new PageResult<>(content, totalElements, pageNumber, pageSize, totalPages);
    }
}
