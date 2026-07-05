package com.paper.reviewer.common;

public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getDefaultMessage());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, new ApiError(errorCode.name(), message));
    }

    public record ApiError(String code, String message) {
    }
}
