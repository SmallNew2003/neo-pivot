package com.jelvin.neopivot.common.api;

/**
 * API 错误详情。
 *
 * <p>主要用于参数校验失败等可结构化表达的错误信息。
 *
 * @author Jelvin
 */
public class ApiErrorDetail {

    private String field;

    private String message;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

