package com.paper.reviewer.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid authentication token"),
    AUTH_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "An account with this email already exists"),
    PAPER_NOT_FOUND(HttpStatus.NOT_FOUND, "Paper not found or access denied"),
    PAPER_INVALID_TYPE(HttpStatus.BAD_REQUEST, "File must be a PDF"),
    PAPER_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "PDF file size exceeds the configured limit"),
    PAPER_PAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PDF page count exceeds the configured limit"),
    PAPER_STORAGE_QUOTA_EXCEEDED(HttpStatus.BAD_REQUEST, "User storage quota exceeded"),
    PDF_EXTRACTION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "PDF text extraction failed"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Review not found or access denied"),
    REVIEW_INVALID_STATUS(HttpStatus.CONFLICT, "Review status does not allow this operation"),
    REVIEW_TEAM_NOT_CONFIRMED(HttpStatus.CONFLICT, "Reviewer team has not been confirmed"),
    AI_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "AI provider request failed"),
    EXPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Export failed"),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Physical file deletion failed"),
    REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
