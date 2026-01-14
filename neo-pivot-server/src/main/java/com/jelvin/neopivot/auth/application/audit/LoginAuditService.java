package com.jelvin.neopivot.auth.application.audit;

import com.jelvin.neopivot.auth.domain.ExternalIdentityProvider;
import com.jelvin.neopivot.auth.domain.LoginGrantType;
import com.jelvin.neopivot.auth.persistence.entity.LoginAuditLogEntity;
import com.jelvin.neopivot.auth.persistence.mapper.LoginAuditLogMapper;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录行为审计服务。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class LoginAuditService {

    private final LoginAuditLogMapper loginAuditLogMapper;

    /**
     * 记录登录结果。
     *
     * @param tenantCode 租户标识
     * @param grantType 登录方式
     * @param provider 外部身份提供方（可空）
     * @param userId 内部用户 ID（可空）
     * @param success 是否成功
     * @param failureReason 失败原因（内部审计用，可空）
     * @param target 登录目标标识（用户名/手机号/邮箱/外部 subject）
     * @param traceId 链路追踪 ID
     * @param ip 来源 IP
     * @param userAgent User-Agent
     */
    public void record(
            String tenantCode,
            LoginGrantType grantType,
            ExternalIdentityProvider provider,
            Long userId,
            boolean success,
            String failureReason,
            String target,
            String traceId,
            String ip,
            String userAgent) {
        LoginAuditLogEntity entity = new LoginAuditLogEntity();
        entity.setTenantCode(normalizeTenantCode(tenantCode));
        entity.setGrantType(grantType == null ? null : grantType.name());
        entity.setProvider(provider == null ? null : provider.name());
        entity.setUserId(userId);
        entity.setSuccess(success);
        entity.setFailureReason(failureReason);
        entity.setTarget(target);
        entity.setTraceId(traceId);
        entity.setIp(ip);
        entity.setUserAgent(userAgent);
        entity.setCreatedAt(Instant.now());
        loginAuditLogMapper.insert(entity);
    }

    private static String normalizeTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return "default";
        }
        return tenantCode.trim();
    }
}
