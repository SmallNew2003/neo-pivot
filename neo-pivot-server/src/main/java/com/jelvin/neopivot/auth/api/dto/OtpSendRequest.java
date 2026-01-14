package com.jelvin.neopivot.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * OTP 发送请求体。
 *
 * <p>用于触发“Challenge”创建与验证码发送：
 * <ul>
 *   <li>短信：{@link OtpChannel#SMS}</li>
 *   <li>邮箱：{@link OtpChannel#EMAIL}</li>
 * </ul>
 *
 * @author Jelvin
 */
@Getter
@Setter
public class OtpSendRequest {

    /**
     * OTP 通道。
     */
    @NotNull
    private OtpChannel channel;

    /**
     * 目标地址：手机号或邮箱。
     */
    @NotBlank
    private String target;

    /**
     * 租户标识（MVP：tenantCode 字符串透传，默认 {@code default}）。
     */
    private String tenantCode;

    /**
     * OTP 通道枚举。
     *
     * @author Jelvin
     */
    public enum OtpChannel {
        SMS,
        EMAIL
    }
}

