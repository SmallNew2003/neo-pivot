package com.jelvin.neopivot.auth.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * OTP 发送响应体。
 *
 * <p>返回 challengeId 供后续校验/登录时使用（前端可选择不展示）。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class OtpSendResponse {

    private String challengeId;

    private Long expiresInSeconds;
}

