package com.jelvin.neopivot.auth.application.login.strategy;

import com.jelvin.neopivot.auth.api.dto.LoginRequest;
import com.jelvin.neopivot.auth.api.dto.OtpSendRequest;
import com.jelvin.neopivot.auth.application.login.LoginAuthResult;
import com.jelvin.neopivot.auth.application.user.ExternalIdentityService;
import com.jelvin.neopivot.auth.application.user.UserProvisioningService;
import com.jelvin.neopivot.auth.application.user.UserQueryService;
import com.jelvin.neopivot.auth.domain.ExternalIdentityProvider;
import com.jelvin.neopivot.auth.domain.LoginGrantType;
import com.jelvin.neopivot.auth.domain.UserRecord;
import com.jelvin.neopivot.auth.exception.AuthLoginFailedException;
import com.jelvin.neopivot.auth.infrastructure.otp.OtpService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 短信验证码登录策略。
 *
 * @author Jelvin
 */
@Component
@RequiredArgsConstructor
public class SmsOtpLoginStrategy implements LoginStrategy {

    private final OtpService otpService;
    private final ExternalIdentityService externalIdentityService;
    private final UserProvisioningService userProvisioningService;
    private final UserQueryService userQueryService;

    @Override
    public LoginGrantType grantType() {
        return LoginGrantType.SMS_OTP;
    }

    @Override
    public LoginAuthResult authenticate(String tenantCode, LoginRequest request) {
        LoginRequest.SmsOtpGrant payload = request == null ? null : request.getSmsOtp();
        String phone = payload == null ? null : payload.getPhone();
        String code = payload == null ? null : payload.getCode();
        if (phone == null || phone.isBlank() || code == null || code.isBlank()) {
            throw new AuthLoginFailedException();
        }

        String normalizedTenantCode = normalizeTenantCode(tenantCode);
        String normalizedPhone = phone.trim();

        otpService.verifyAndConsume(normalizedTenantCode, OtpSendRequest.OtpChannel.SMS, normalizedPhone, code);

        Long userId = resolveOrCreateUserId(normalizedTenantCode, ExternalIdentityProvider.SMS_OTP, normalizedPhone);
        Optional<UserRecord> user = userQueryService.findById(userId);
        if (user.isEmpty()) {
            throw new AuthLoginFailedException();
        }
        return new LoginAuthResult(user.get(), ExternalIdentityProvider.SMS_OTP, normalizedPhone);
    }

    private Long resolveOrCreateUserId(String tenantCode, ExternalIdentityProvider provider, String externalSubject) {
        Optional<Long> existing = externalIdentityService.findUserId(tenantCode, provider, externalSubject);
        if (existing.isPresent()) {
            return existing.get();
        }
        String username = buildExternalUsername(tenantCode, provider, externalSubject);
        Long userId = userProvisioningService.createUser(username);
        externalIdentityService.createMapping(tenantCode, provider, externalSubject, userId);
        return userId;
    }

    private static String buildExternalUsername(String tenantCode, ExternalIdentityProvider provider, String subject) {
        String normalizedTenantCode = normalizeTenantCode(tenantCode);
        if ("default".equals(normalizedTenantCode)) {
            return subject;
        }
        return normalizedTenantCode + ":" + provider.name().toLowerCase() + ":" + subject;
    }

    private static String normalizeTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return "default";
        }
        return tenantCode.trim();
    }
}
