package com.goldtrading.backend.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final Object errors;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.errors = null;
    }

    public BusinessException(String code, String message, Object errors) {
        super(message);
        this.code = code;
        this.errors = errors;
    }
}

