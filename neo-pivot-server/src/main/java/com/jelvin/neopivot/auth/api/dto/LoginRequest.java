package com.jelvin.neopivot.auth.api.dto;

import com.jelvin.neopivot.auth.domain.LoginGrantType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一登录请求体（单端点 + grantType）。
 *
 * <p>说明：
 * <ul>
 *   <li>所有登录方式统一提交到 {@code POST /api/auth/login}</li>
 *   <li>由 {@code grantType} 决定使用哪一组参数</li>
 *   <li>登录成功后统一签发用户级 JWT（RS256），且 {@code sub} 为内部用户 ID</li>
 * </ul>
 *
 * @author Jelvin
 */
@Getter
@Setter
public class LoginRequest {

    /**
     * 登录方式。
     */
    @NotNull
    private LoginGrantType grantType;

    /**
     * 租户标识（MVP：tenantCode 字符串透传，默认 {@code default}）。
     */
    private String tenantCode;

    /**
     * 用户名/密码登录参数。
     */
    @Valid
    private PasswordGrant password;

    /**
     * 短信验证码登录参数。
     */
    @Valid
    private SmsOtpGrant smsOtp;

    /**
     * 邮箱验证码登录参数。
     */
    @Valid
    private EmailOtpGrant emailOtp;

    /**
     * 微信授权码登录参数。
     */
    @Valid
    private WechatCodeGrant wechatCode;

    /**
     * 校验 grantType 与参数结构匹配，避免字段互斥导致的复杂校验分支。
     *
     * @return grantType 对应的参数存在则为 true
     */
    @AssertTrue(message = "grantType 对应的登录参数缺失")
    public boolean isGrantPayloadPresent() {
        if (grantType == null) {
            return false;
        }
        return switch (grantType) {
            case PASSWORD -> password != null;
            case SMS_OTP -> smsOtp != null;
            case EMAIL_OTP -> emailOtp != null;
            case WECHAT_CODE -> wechatCode != null;
        };
    }

    /**
     * 用户名/密码登录参数。
     *
     * @author Jelvin
     */
    @Getter
    @Setter
    public static class PasswordGrant {

        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    /**
     * 短信 OTP 登录参数。
     *
     * @author Jelvin
     */
    @Getter
    @Setter
    public static class SmsOtpGrant {

        @NotBlank
        private String phone;

        @NotBlank
        private String code;
    }

    /**
     * 邮箱 OTP 登录参数。
     *
     * @author Jelvin
     */
    @Getter
    @Setter
    public static class EmailOtpGrant {

        @NotBlank
        private String email;

        @NotBlank
        private String code;
    }

    /**
     * 微信授权码登录参数。
     *
     * @author Jelvin
     */
    @Getter
    @Setter
    public static class WechatCodeGrant {

        @NotBlank
        private String code;
    }
}
