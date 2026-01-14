package com.jelvin.neopivot.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 登录失败异常（对外保持统一语义，避免可枚举信息泄露）。
 *
 * @author Jelvin
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "认证失败")
public class AuthLoginFailedException extends RuntimeException {}
