package com.paper.reviewer.storage.service;

/**
 * Database-facing boundary used by storage quota checks. Implementations must sum
 * {@code file_size} for the user's papers.
 */
@FunctionalInterface
public interface PaperStorageUsageReader {

    long getActivePaperFileSize(long userId);
}
