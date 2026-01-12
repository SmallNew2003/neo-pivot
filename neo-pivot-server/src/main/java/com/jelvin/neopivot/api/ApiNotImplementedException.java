package com.jelvin.neopivot.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * API 未实现异常（用于骨架阶段占位）。
 *
 * @author Jelvin
 */
@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class ApiNotImplementedException extends RuntimeException {

    /**
     * 构造函数。
     *
     * @param message 提示信息
     */
    public ApiNotImplementedException(String message) {
        super(message);
    }
}

