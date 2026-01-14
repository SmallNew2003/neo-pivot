package com.jelvin.neopivot.auth.infrastructure.otp;

import com.jelvin.neopivot.auth.api.dto.OtpSendRequest;
import com.jelvin.neopivot.auth.config.AuthProperties;
import com.jelvin.neopivot.auth.exception.OtpRateLimitedException;
import com.jelvin.neopivot.auth.exception.OtpVerificationFailedException;
import com.jelvin.neopivot.auth.persistence.entity.OtpChallengeEntity;
import com.jelvin.neopivot.auth.persistence.mapper.OtpChallengeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * OTP Challenge/Verify 服务。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom random = new SecureRandom();

    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;
    private final OtpChallengeMapper otpChallengeMapper;

    /**
     * 创建并发送 OTP challenge（最小实现：创建 challenge + 频控 + TTL）。
     *
     * @param tenantCode 租户标识
     * @param channel 通道
     * @param target 目标地址（手机号或邮箱）
     * @return challengeId
     */
    public OtpIssueResult issue(String tenantCode, OtpSendRequest.OtpChannel channel, String target) {
        if (channel == null) {
            throw new IllegalArgumentException("channel 不能为空");
        }
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target 不能为空");
        }

        String normalizedTenantCode = normalizeTenantCode(tenantCode);
        String normalizedTarget = target.trim();

        Instant now = Instant.now();
        OtpChallengeEntity latest = findLatestChallenge(normalizedTenantCode, channel, normalizedTarget);
        if (latest != null && latest.getLastSentAt() != null) {
            Instant nextAllowedAt = latest.getLastSentAt().plus(authProperties.getOtp().getSendInterval());
            if (nextAllowedAt.isAfter(now)) {
                throw new OtpRateLimitedException();
            }
        }

        String code = generateNumericCode(6);
        String challengeId = UUID.randomUUID().toString().replace("-", "");

        OtpChallengeEntity entity = new OtpChallengeEntity();
        entity.setTenantCode(normalizedTenantCode);
        entity.setChannel(channel.name());
        entity.setTarget(normalizedTarget);
        entity.setChallengeId(challengeId);
        entity.setCodeHash(passwordEncoder.encode(code));
        entity.setExpiresAt(now.plus(authProperties.getOtp().getTtl()));
        entity.setUsedAt(null);
        entity.setVerifyAttempts(0);
        entity.setLastSentAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        otpChallengeMapper.insert(entity);

        return new OtpIssueResult(challengeId, authProperties.getOtp().getTtl().toSeconds());
    }

    /**
     * 校验并消费 OTP（一次性使用 + 失败次数上限）。
     *
     * @param tenantCode 租户标识
     * @param channel 通道
     * @param target 目标地址
     * @param code 用户输入验证码
     */
    public void verifyAndConsume(String tenantCode, OtpSendRequest.OtpChannel channel, String target, String code) {
        if (channel == null) {
            throw new IllegalArgumentException("channel 不能为空");
        }
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target 不能为空");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code 不能为空");
        }

        String normalizedTenantCode = normalizeTenantCode(tenantCode);
        String normalizedTarget = target.trim();

        Instant now = Instant.now();
        OtpChallengeEntity latest = findLatestChallenge(normalizedTenantCode, channel, normalizedTarget);
        if (latest == null) {
            throw new OtpVerificationFailedException();
        }
        if (latest.getUsedAt() != null) {
            throw new OtpVerificationFailedException();
        }
        if (latest.getExpiresAt() != null && latest.getExpiresAt().isBefore(now)) {
            throw new OtpVerificationFailedException();
        }

        int attempts = latest.getVerifyAttempts() == null ? 0 : latest.getVerifyAttempts();
        int maxAttempts = authProperties.getOtp().getMaxVerifyAttempts() == null ? 5 : authProperties.getOtp().getMaxVerifyAttempts();
        if (attempts >= maxAttempts) {
            throw new OtpVerificationFailedException();
        }

        boolean matched = latest.getCodeHash() != null && passwordEncoder.matches(code.trim(), latest.getCodeHash());
        if (!matched) {
            latest.setVerifyAttempts(attempts + 1);
            latest.setUpdatedAt(now);
            otpChallengeMapper.update(latest);
            throw new OtpVerificationFailedException();
        }

        latest.setUsedAt(now);
        latest.setUpdatedAt(now);
        otpChallengeMapper.update(latest);
    }

    private OtpChallengeEntity findLatestChallenge(String tenantCode, OtpSendRequest.OtpChannel channel, String target) {
        QueryWrapper query =
                QueryWrapper.create()
                        .where("tenant_code = ?", tenantCode)
                        .and("channel = ?", channel.name())
                        .and("target = ?", target)
                        .orderBy("created_at desc");
        List<OtpChallengeEntity> list = otpChallengeMapper.selectListByQuery(query);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    private static String generateNumericCode(int length) {
        int bound = (int) Math.pow(10, length);
        int floor = (int) Math.pow(10, length - 1);
        int number = floor + random.nextInt(bound - floor);
        return String.valueOf(number);
    }

    private static String normalizeTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return "default";
        }
        return tenantCode.trim();
    }

    /**
     * OTP challenge 签发结果。
     *
     * @param challengeId 挑战 ID
     * @param expiresInSeconds 剩余秒数
     * @author Jelvin
     */
    public record OtpIssueResult(String challengeId, long expiresInSeconds) {}
}
