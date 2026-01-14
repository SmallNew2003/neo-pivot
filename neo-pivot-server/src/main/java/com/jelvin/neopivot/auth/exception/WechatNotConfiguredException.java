package com.jelvin.neopivot.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 微信登录未配置异常。
 *
 * @author Jelvin
 */
@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED, reason = "微信登录未配置")
public class WechatNotConfiguredException extends RuntimeException {}
