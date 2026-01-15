package com.jelvin.neopivot.auth.infrastructure.otp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jelvin.neopivot.auth.api.dto.OtpSendRequest;
import com.jelvin.neopivot.auth.config.AuthProperties;
import com.jelvin.neopivot.auth.exception.OtpRateLimitedException;
import com.jelvin.neopivot.auth.exception.OtpVerificationFailedException;
import com.jelvin.neopivot.auth.persistence.entity.OtpChallengeEntity;
import com.jelvin.neopivot.auth.persistence.mapper.OtpChallengeMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@link OtpService} 单测。
 *
 * <p>覆盖验证码 Challenge/Verify 的关键语义：
 * <ul>
 *   <li>发送频控：发送间隔内重复发送返回 429</li>
 *   <li>一次性使用：校验成功后写入 usedAt</li>
 *   <li>失败次数：校验失败会递增 verifyAttempts，达到上限后不可继续尝试</li>
 * </ul>
 *
 * @author Jelvin
 */
@ExtendWith(MockitoExtension.class)
public class OtpServiceTest {

    @Mock
    private OtpChallengeMapper otpChallengeMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private OtpService otpService;

    @Captor
    private ArgumentCaptor<OtpChallengeEntity> challengeCaptor;

    @Test
    void shouldRateLimitWhenSendIntervalNotElapsed() {
        AuthProperties authProperties = buildAuthProperties(Duration.ofMinutes(5), Duration.ofSeconds(60), 5);
        otpService = new OtpService(authProperties, passwordEncoder, otpChallengeMapper);

        OtpChallengeEntity latest = new OtpChallengeEntity();
        latest.setTenantCode("default");
        latest.setChannel("SMS");
        latest.setTarget("13800000000");
        latest.setLastSentAt(Instant.now());

        when(otpChallengeMapper.selectListByQuery(any())).thenReturn(List.of(latest));

        assertThrows(
                OtpRateLimitedException.class,
                () -> otpService.issue("default", OtpSendRequest.OtpChannel.SMS, "13800000000"));
    }

    @Test
    void shouldCreateChallengeWhenAllowed() {
        AuthProperties authProperties = buildAuthProperties(Duration.ofMinutes(5), Duration.ofSeconds(1), 5);
        otpService = new OtpService(authProperties, passwordEncoder, otpChallengeMapper);

        OtpChallengeEntity latest = new OtpChallengeEntity();
        latest.setTenantCode("default");
        latest.setChannel("SMS");
        latest.setTarget("13800000000");
        latest.setLastSentAt(Instant.now().minusSeconds(10));
        when(otpChallengeMapper.selectListByQuery(any())).thenReturn(List.of(latest));

        OtpService.OtpIssueResult result = otpService.issue("default", OtpSendRequest.OtpChannel.SMS, "13800000000");

        assertTrue(result.challengeId() != null && !result.challengeId().isBlank());
        assertNotNull(result.expiresInSeconds());
        verify(otpChallengeMapper).insert(any());
    }

    @Test
    void shouldConsumeOtpOnVerifySuccess() {
        AuthProperties authProperties = buildAuthProperties(Duration.ofMinutes(5), Duration.ofSeconds(60), 5);
        otpService = new OtpService(authProperties, passwordEncoder, otpChallengeMapper);

        OtpChallengeEntity latest = new OtpChallengeEntity();
        latest.setTenantCode("default");
        latest.setChannel("SMS");
        latest.setTarget("13800000000");
        latest.setChallengeId("c1");
        latest.setCodeHash(passwordEncoder.encode("123456"));
        latest.setExpiresAt(Instant.now().plusSeconds(300));
        latest.setUsedAt(null);
        latest.setVerifyAttempts(0);
        latest.setLastSentAt(Instant.now());

        when(otpChallengeMapper.selectListByQuery(any())).thenReturn(List.of(latest));

        otpService.verifyAndConsume("default", OtpSendRequest.OtpChannel.SMS, "13800000000", "123456");

        verify(otpChallengeMapper).update(challengeCaptor.capture());
        OtpChallengeEntity updated = challengeCaptor.getValue();
        assertNotNull(updated.getUsedAt());
    }

    @Test
    void shouldIncreaseAttemptsOnVerifyFailure() {
        AuthProperties authProperties = buildAuthProperties(Duration.ofMinutes(5), Duration.ofSeconds(60), 2);
        otpService = new OtpService(authProperties, passwordEncoder, otpChallengeMapper);

        OtpChallengeEntity latest = new OtpChallengeEntity();
        latest.setTenantCode("default");
        latest.setChannel("SMS");
        latest.setTarget("13800000000");
        latest.setChallengeId("c1");
        latest.setCodeHash(passwordEncoder.encode("123456"));
        latest.setExpiresAt(Instant.now().plusSeconds(300));
        latest.setUsedAt(null);
        latest.setVerifyAttempts(0);
        latest.setLastSentAt(Instant.now());

        when(otpChallengeMapper.selectListByQuery(any())).thenReturn(List.of(latest));

        assertThrows(
                OtpVerificationFailedException.class,
                () -> otpService.verifyAndConsume("default", OtpSendRequest.OtpChannel.SMS, "13800000000", "000000"));

        verify(otpChallengeMapper).update(challengeCaptor.capture());
        OtpChallengeEntity updated = challengeCaptor.getValue();
        assertNotNull(updated.getVerifyAttempts());
        assertTrue(updated.getVerifyAttempts() >= 1);
        assertThrows(
                OtpVerificationFailedException.class,
                () -> otpService.verifyAndConsume("default", OtpSendRequest.OtpChannel.SMS, "13800000000", "000000"));
    }

    private static AuthProperties buildAuthProperties(Duration ttl, Duration sendInterval, int maxVerifyAttempts) {
        AuthProperties props = new AuthProperties();
        AuthProperties.OtpProperties otp = new AuthProperties.OtpProperties();
        otp.setTtl(ttl);
        otp.setSendInterval(sendInterval);
        otp.setMaxVerifyAttempts(maxVerifyAttempts);
        props.setOtp(otp);
        return props;
    }
}
