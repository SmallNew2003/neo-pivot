package com.jelvin.neopivot.auth.domain;

/**
 * 登录授权类型（grantType）。
 *
 * <p>用于统一 {@code POST /api/auth/login} 的登录方式选择：
 * <ul>
 *   <li>{@link #PASSWORD}：用户名/密码</li>
 *   <li>{@link #SMS_OTP}：短信验证码</li>
 *   <li>{@link #EMAIL_OTP}：邮箱验证码</li>
 *   <li>{@link #WECHAT_CODE}：微信 OAuth2 授权码</li>
 * </ul>
 *
 * @author Jelvin
 */
public enum LoginGrantType {
    PASSWORD,
    SMS_OTP,
    EMAIL_OTP,
    WECHAT_CODE
}

