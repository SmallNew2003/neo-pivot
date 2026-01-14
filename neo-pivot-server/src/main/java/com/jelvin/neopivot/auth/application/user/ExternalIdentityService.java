package com.jelvin.neopivot.auth.application.user;

import com.jelvin.neopivot.auth.domain.ExternalIdentityProvider;
import com.jelvin.neopivot.auth.persistence.entity.ExternalIdentityEntity;
import com.jelvin.neopivot.auth.persistence.mapper.ExternalIdentityMapper;
import com.mybatisflex.core.query.QueryWrapper;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 外部身份映射服务。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class ExternalIdentityService {

    private final ExternalIdentityMapper externalIdentityMapper;

    /**
     * 按外部身份解析内部用户 ID。
     *
     * @param tenantCode 租户标识
     * @param provider 提供方
     * @param externalSubject 外部主体标识
     * @return 内部用户 ID
     */
    public Optional<Long> findUserId(String tenantCode, ExternalIdentityProvider provider, String externalSubject) {
        if (provider == null || externalSubject == null || externalSubject.isBlank()) {
            return Optional.empty();
        }
        QueryWrapper query =
                QueryWrapper.create()
                        .where("tenant_code = ?", normalizeTenantCode(tenantCode))
                        .and("provider = ?", provider.name())
                        .and("external_subject = ?", externalSubject.trim());
        ExternalIdentityEntity entity = externalIdentityMapper.selectOneByQuery(query);
        if (entity == null || entity.getUserId() == null) {
            return Optional.empty();
        }
        return Optional.of(entity.getUserId());
    }

    /**
     * 创建外部身份映射。
     *
     * @param tenantCode 租户标识
     * @param provider 提供方
     * @param externalSubject 外部主体标识
     * @param userId 内部用户 ID
     */
    public void createMapping(String tenantCode, ExternalIdentityProvider provider, String externalSubject, Long userId) {
        if (provider == null) {
            throw new IllegalArgumentException("provider 不能为空");
        }
        if (externalSubject == null || externalSubject.isBlank()) {
            throw new IllegalArgumentException("externalSubject 不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }

        ExternalIdentityEntity entity = new ExternalIdentityEntity();
        entity.setTenantCode(normalizeTenantCode(tenantCode));
        entity.setProvider(provider.name());
        entity.setExternalSubject(externalSubject.trim());
        entity.setUserId(userId);
        entity.setCreatedAt(Instant.now());
        externalIdentityMapper.insert(entity);
    }

    private static String normalizeTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return "default";
        }
        return tenantCode.trim();
    }
}
