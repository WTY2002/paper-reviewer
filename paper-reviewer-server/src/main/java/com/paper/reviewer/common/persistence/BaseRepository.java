package com.paper.reviewer.common.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/** Shared persistence boundary; feature services depend on a table-specific subtype. */
public interface BaseRepository<T> extends BaseMapper<T> {
}
