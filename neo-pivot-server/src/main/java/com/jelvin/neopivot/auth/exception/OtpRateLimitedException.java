package com.jelvin.neopivot.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * OTP 发送频控异常。
 *
 * @author Jelvin
 */
@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS, reason = "请求过于频繁，请稍后再试")
public class OtpRateLimitedException extends RuntimeException {}
