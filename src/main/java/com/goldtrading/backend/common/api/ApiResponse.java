package com.goldtrading.backend.common.api;

import lombok.Builder;

@Builder
public record ApiResponse<T>(boolean success, T data, String code, String message, Object errors) {
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static ApiResponse<Void> error(String code, String message, Object errors) {
        return ApiResponse.<Void>builder().success(false).code(code).message(message).errors(errors).build();
    }
}

