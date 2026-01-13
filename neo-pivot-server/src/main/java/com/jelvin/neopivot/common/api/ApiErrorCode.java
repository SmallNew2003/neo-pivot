package com.jelvin.neopivot.common.api;

/**
 * API 错误码。
 *
 * <p>用于统一响应体（Envelope）中 {@code code} 字段的最小集合。
 *
 * @author Jelvin
 */
public enum ApiErrorCode {
    OK("0"),
    BAD_REQUEST("BAD_REQUEST"),
    VALIDATION_ERROR("VALIDATION_ERROR"),
    UNAUTHORIZED("UNAUTHORIZED"),
    FORBIDDEN("FORBIDDEN"),
    NOT_FOUND("NOT_FOUND"),
    CONFLICT("CONFLICT"),
    GONE("GONE"),
    NOT_IMPLEMENTED("NOT_IMPLEMENTED"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String code;

    ApiErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

