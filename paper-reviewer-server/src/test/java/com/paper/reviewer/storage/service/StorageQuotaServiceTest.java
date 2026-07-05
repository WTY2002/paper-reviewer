package com.paper.reviewer.storage.service;

import com.paper.reviewer.storage.domain.StorageQuotaExceededException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageQuotaServiceTest {

    private static final long MIB = 1024L * 1024L;

    @Test
    void allowsUploadWhenTotalEqualsFiveHundredMebibytes() {
        StorageQuotaService service = new StorageQuotaService(userId -> 480L * MIB, 500, MIB);

        assertDoesNotThrow(() -> service.validateCanStore(1, 20L * MIB));
        assertEquals(500L * MIB, service.getMaximumBytes());
    }

    @Test
    void rejectsUploadWhenTotalExceedsFiveHundredMebibytes() {
        StorageQuotaService service = new StorageQuotaService(userId -> 480L * MIB, 500, MIB);

        assertThrows(StorageQuotaExceededException.class,
                () -> service.validateCanStore(1, 20L * MIB + 1));
    }

    @Test
    void rejectsWhenExistingUsageAlreadyExceedsQuotaWithoutOverflow() {
        StorageQuotaService service = new StorageQuotaService(userId -> Long.MAX_VALUE, 500, MIB);

        assertThrows(StorageQuotaExceededException.class, () -> service.validateCanStore(1, Long.MAX_VALUE));
    }

    @Test
    void readsOnlyUsageProvidedByActivePaperBoundary() {
        StorageQuotaService service = new StorageQuotaService(userId -> userId == 7 ? 1234 : 0, 500, MIB);

        assertEquals(1234, service.getUsedBytes(7));
    }

    @Test
    void rejectsInvalidUsageAndInput() {
        StorageQuotaService invalidUsage = new StorageQuotaService(userId -> -1, 500, MIB);

        assertThrows(IllegalStateException.class, () -> invalidUsage.getUsedBytes(1));
        assertThrows(IllegalArgumentException.class, () -> invalidUsage.validateCanStore(1, -1));
        assertThrows(IllegalArgumentException.class, () -> invalidUsage.getUsedBytes(0));
    }
}
