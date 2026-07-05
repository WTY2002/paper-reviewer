package com.paper.reviewer.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void convertsBusinessExceptionToItsStableErrorCode() {
        ResponseEntity<ApiResponse<Void>> entity = handler.handleBusinessException(
                new BusinessException(ErrorCode.REVIEW_INVALID_STATUS, "Cannot start this review"));

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().success()).isFalse();
        assertThat(entity.getBody().error().code()).isEqualTo("REVIEW_INVALID_STATUS");
        assertThat(entity.getBody().error().message()).isEqualTo("Cannot start this review");
    }

    @Test
    void convertsMultipartLimitExceptionToPaperSizeError() {
        ResponseEntity<ApiResponse<Void>> entity = handler.handleMaxUploadSizeException(
                new MaxUploadSizeExceededException(20L * 1024 * 1024));

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(entity.getBody().error().code()).isEqualTo("PAPER_FILE_TOO_LARGE");
    }

    @Test
    void hidesUnexpectedExceptionDetails() {
        ResponseEntity<ApiResponse<Void>> entity = handler.handleUnexpectedException(
                new IllegalStateException("database password must not leak"));

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(entity.getBody().error().code()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(entity.getBody().error().message()).doesNotContain("password");
    }
}
