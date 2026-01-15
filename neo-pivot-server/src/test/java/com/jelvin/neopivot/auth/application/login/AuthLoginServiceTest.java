package com.jelvin.neopivot.auth.application.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jelvin.neopivot.auth.api.dto.LoginRequest;
import com.jelvin.neopivot.auth.application.audit.LoginAuditService;
import com.jelvin.neopivot.auth.application.login.strategy.LoginStrategy;
import com.jelvin.neopivot.auth.application.login.strategy.LoginStrategyFactory;
import com.jelvin.neopivot.auth.config.AuthProperties;
import com.jelvin.neopivot.auth.domain.LoginGrantType;
import com.jelvin.neopivot.auth.domain.UserRecord;
import com.jelvin.neopivot.auth.exception.AuthLoginFailedException;
import com.jelvin.neopivot.auth.infrastructure.jwt.JwtTokenService;
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
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * {@link AuthLoginService} 单测。
 *
 * <p>覆盖统一登录的关键路径：
 * <ul>
 *   <li>成功：按 grantType 选择策略 -> 签发 JWT -> 写入成功审计</li>
 *   <li>失败：抛出异常 -> 写入失败审计（对外仍保持统一失败语义）</li>
 * </ul>
 *
 * @author Jelvin
 */
@ExtendWith(MockitoExtension.class)
public class AuthLoginServiceTest {

    @Mock
    private AuthProperties authProperties;

    @Mock
    private LoginStrategyFactory loginStrategyFactory;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private LoginAuditService loginAuditService;

    @Mock
    private LoginStrategy loginStrategy;

    @InjectMocks
    private AuthLoginService authLoginService;

    @Captor
    private ArgumentCaptor<String> failureReasonCaptor;

    @Test
    void shouldIssueJwtAndRecordSuccessAudit() {
        when(authProperties.getTokenTtl()).thenReturn(Duration.ofSeconds(1800));

        LoginRequest request = buildPasswordRequest("t1", "demo", "demo");
        UserRecord user = new UserRecord(1L, "demo", "hash", List.of("USER"));
        Jwt jwt =
                Jwt.withTokenValue("token")
                        .header("alg", "RS256")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(1800))
                        .claim("sub", "1")
                        .build();

        when(loginStrategyFactory.get(LoginGrantType.PASSWORD)).thenReturn(loginStrategy);
        when(loginStrategy.authenticate(eq("t1"), eq(request))).thenReturn(new LoginAuthResult(user, null, "demo"));
        when(jwtTokenService.issueToken(eq(user), eq("t1"))).thenReturn(jwt);

        var response = authLoginService.login(request, "trace-1", "127.0.0.1", "ua");

        assertEquals("token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1800L, response.getExpiresInSeconds());
        assertNotNull(response.getUser());
        assertEquals("1", response.getUser().getId());
        assertEquals("demo", response.getUser().getUsername());
        assertEquals(List.of("USER"), response.getUser().getRoles());

        verify(loginAuditService)
                .record(
                        eq("t1"),
                        eq(LoginGrantType.PASSWORD),
                        eq(null),
                        eq(1L),
                        eq(true),
                        eq(null),
                        eq("demo"),
                        eq("trace-1"),
                        eq("127.0.0.1"),
                        eq("ua"));
    }

    @Test
    void shouldRecordFailureAuditAndRethrow() {
        LoginRequest request = buildPasswordRequest("t1", "demo", "demo");

        when(loginStrategyFactory.get(LoginGrantType.PASSWORD)).thenThrow(new AuthLoginFailedException());

        assertThrows(AuthLoginFailedException.class, () -> authLoginService.login(request, "trace-2", "127.0.0.1", "ua"));

        verify(loginAuditService)
                .record(
                        eq("t1"),
                        eq(LoginGrantType.PASSWORD),
                        eq(null),
                        eq(null),
                        eq(false),
                        failureReasonCaptor.capture(),
                        eq("demo"),
                        eq("trace-2"),
                        eq("127.0.0.1"),
                        eq("ua"));
        assertEquals("AuthLoginFailedException", failureReasonCaptor.getValue());
    }

    private static LoginRequest buildPasswordRequest(String tenantCode, String username, String password) {
        LoginRequest.PasswordGrant grant = new LoginRequest.PasswordGrant();
        grant.setUsername(username);
        grant.setPassword(password);

        LoginRequest request = new LoginRequest();
        request.setTenantCode(tenantCode);
        request.setGrantType(LoginGrantType.PASSWORD);
        request.setPassword(grant);
        return request;
    }
}

