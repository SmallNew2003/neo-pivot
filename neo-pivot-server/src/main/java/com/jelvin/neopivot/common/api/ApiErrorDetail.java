package com.jelvin.neopivot.common.api;

import lombok.Getter;
import lombok.Setter;

/**
 * API 错误详情。
 *
 * <p>主要用于参数校验失败等可结构化表达的错误信息。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class ApiErrorDetail {

    private String field;

    private String message;
}
