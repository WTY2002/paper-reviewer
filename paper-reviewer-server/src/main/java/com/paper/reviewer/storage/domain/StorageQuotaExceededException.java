package com.paper.reviewer.storage.domain;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;

public class StorageQuotaExceededException extends BusinessException {

    public static final String ERROR_CODE = "PAPER_STORAGE_QUOTA_EXCEEDED";

    public StorageQuotaExceededException(long quotaBytes, long usedBytes, long incomingBytes) {
        super(ErrorCode.PAPER_STORAGE_QUOTA_EXCEEDED, "User storage quota exceeded: quota=" + quotaBytes
                + ", used=" + usedBytes + ", incoming=" + incomingBytes);
    }
}
