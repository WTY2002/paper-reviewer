package com.paper.reviewer.storage.service;

import com.paper.reviewer.storage.domain.StorageQuotaExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(PaperStorageUsageReader.class)
public class StorageQuotaService {

    private static final long BYTES_PER_MEBIBYTE = 1024L * 1024L;

    private final PaperStorageUsageReader usageReader;
    private final long maximumBytes;

    @Autowired
    public StorageQuotaService(
            PaperStorageUsageReader usageReader,
            @Value("${app.upload.max-user-storage-mb:500}") long maximumMegabytes) {
        this(usageReader, maximumMegabytes, BYTES_PER_MEBIBYTE);
    }

    StorageQuotaService(PaperStorageUsageReader usageReader, long maximumMegabytes, long bytesPerMegabyte) {
        if (maximumMegabytes <= 0) {
            throw new IllegalArgumentException("maximumMegabytes must be positive");
        }
        this.usageReader = usageReader;
        try {
            this.maximumBytes = Math.multiplyExact(maximumMegabytes, bytesPerMegabyte);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Storage quota is too large", exception);
        }
    }

    public long getUsedBytes(long userId) {
        requirePositive(userId, "userId");
        long usedBytes = usageReader.getActivePaperFileSize(userId);
        if (usedBytes < 0) {
            throw new IllegalStateException("Paper storage usage cannot be negative");
        }
        return usedBytes;
    }

    public void validateCanStore(long userId, long incomingBytes) {
        if (incomingBytes < 0) {
            throw new IllegalArgumentException("incomingBytes cannot be negative");
        }
        long usedBytes = getUsedBytes(userId);
        if (usedBytes > maximumBytes || incomingBytes > maximumBytes - usedBytes) {
            throw new StorageQuotaExceededException(maximumBytes, usedBytes, incomingBytes);
        }
    }

    public long getMaximumBytes() {
        return maximumBytes;
    }

    private static void requirePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
