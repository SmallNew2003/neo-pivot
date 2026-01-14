package com.jelvin.neopivot.auth.application.login;

import com.jelvin.neopivot.auth.api.dto.LoginRequest;
import com.jelvin.neopivot.auth.api.dto.LoginResponse;
import com.jelvin.neopivot.auth.application.audit.LoginAuditService;
import com.jelvin.neopivot.auth.application.login.strategy.LoginStrategy;
import com.jelvin.neopivot.auth.application.login.strategy.LoginStrategyFactory;
import com.jelvin.neopivot.auth.config.AuthProperties;
import com.jelvin.neopivot.auth.domain.LoginGrantType;
import com.jelvin.neopivot.auth.domain.UserRecord;
import com.jelvin.neopivot.auth.exception.AuthLoginFailedException;
import com.jelvin.neopivot.auth.infrastructure.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 统一登录编排服务。
 *
 * <p>职责：
 * <ul>
 *   <li>按 {@code grantType} 选择登录策略并完成认证</li>
 *   <li>认证成功后签发用户级 JWT（RS256）</li>
 *   <li>记录登录行为日志（成功/失败）用于审计与风控</li>
 * </ul>
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class AuthLoginService {

    private final AuthProperties authProperties;
    private final LoginStrategyFactory loginStrategyFactory;
    private final JwtTokenService jwtTokenService;
    private final LoginAuditService loginAuditService;

    /**
     * 执行统一登录并签发 JWT。
     *
     * @param request 登录请求
     * @param traceId 链路追踪 ID
     * @param ip 来源 IP（可空）
     * @param userAgent User-Agent（可空）
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest request, String traceId, String ip, String userAgent) {
        if (request == null) {
            throw new AuthLoginFailedException();
        }

        LoginGrantType grantType = request.getGrantType();
        String tenantCode = request.getTenantCode();
        String auditTarget = resolveAuditTarget(grantType, request);

        try {
            LoginStrategy strategy = loginStrategyFactory.get(grantType);
            LoginAuthResult authResult = strategy.authenticate(tenantCode, request);
            UserRecord user = authResult.user();
            if (user == null) {
                throw new AuthLoginFailedException();
            }

            var jwt = jwtTokenService.issueToken(user, tenantCode);

            LoginResponse response = new LoginResponse();
            response.setAccessToken(jwt.getTokenValue());
            response.setTokenType("Bearer");
            response.setExpiresInSeconds(authProperties.getTokenTtl().toSeconds());

            LoginResponse.UserView userView = new LoginResponse.UserView();
            userView.setId(String.valueOf(user.id()));
            userView.setUsername(user.username());
            userView.setRoles(user.roles());
            response.setUser(userView);

            loginAuditService.record(
                    tenantCode,
                    grantType,
                    authResult.provider(),
                    user.id(),
                    true,
                    null,
                    authResult.target(),
                    traceId,
                    ip,
                    userAgent);
            return response;
        } catch (RuntimeException ex) {
            loginAuditService.record(
                    tenantCode,
                    grantType,
                    null,
                    null,
                    false,
                    ex.getClass().getSimpleName(),
                    auditTarget,
                    traceId,
                    ip,
                    userAgent);
            throw ex;
        }
    }

    private static String resolveAuditTarget(LoginGrantType grantType, LoginRequest request) {
        if (grantType == null || request == null) {
            return null;
        }
        return switch (grantType) {
            case PASSWORD -> request.getPassword() == null ? null : request.getPassword().getUsername();
            case SMS_OTP -> request.getSmsOtp() == null ? null : request.getSmsOtp().getPhone();
            case EMAIL_OTP -> request.getEmailOtp() == null ? null : request.getEmailOtp().getEmail();
            case WECHAT_CODE -> "wechat";
        };
    }
}
