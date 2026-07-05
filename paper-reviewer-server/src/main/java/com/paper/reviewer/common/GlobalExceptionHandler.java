package com.paper.reviewer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode code = exception.getErrorCode();
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.error(code, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse(ErrorCode.REQUEST_VALIDATION_FAILED.getDefaultMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.REQUEST_VALIDATION_FAILED, message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeException(
            MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(ErrorCode.PAPER_FILE_TOO_LARGE.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.PAPER_FILE_TOO_LARGE));
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleDisconnectedAsyncResponse(AsyncRequestNotUsableException exception) {
        // The browser closed an SSE connection. Its response is already committed,
        // so attempting to serialize the normal JSON error envelope would only
        // produce a second HttpMessageNotWritableException.
        log.debug("Async response is no longer usable (client disconnected)", exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unhandled request exception", exception);
        ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(code));
    }
}
