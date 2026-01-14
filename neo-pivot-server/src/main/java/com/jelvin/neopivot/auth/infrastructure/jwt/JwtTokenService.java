package com.jelvin.neopivot.auth.infrastructure.jwt;

import com.jelvin.neopivot.auth.config.AuthProperties;
import com.jelvin.neopivot.auth.domain.UserRecord;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * JWT 签发服务。
 *
 * <p>用于标准登录接口签发用户级 access token（RS256）。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final AuthProperties authProperties;
    private final JwtEncoder jwtEncoder;
    private final JwtKeyService jwtKeyService;

    /**
     * 签发用户级 JWT。
     *
     * @param user 用户记录
     * @param tenantCode 租户标识（写入 tid claim）
     * @return JWT
     */
    public Jwt issueToken(UserRecord user, String tenantCode) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(authProperties.getTokenTtl());

        String normalizedTenantCode = normalizeTenantCode(tenantCode);
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(authProperties.getIssuer())
                        .audience(List.of(authProperties.getAudience()))
                        .issuedAt(now)
                        .expiresAt(expiresAt)
                        .subject(String.valueOf(user.id()))
                        .id(UUID.randomUUID().toString())
                        .claim("roles", user.roles())
                        .claim("tid", normalizedTenantCode)
                        .build();

        JwsHeader header =
                JwsHeader.with(SignatureAlgorithm.RS256).keyId(jwtKeyService.keyId()).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
    }

    /**
     * 签发用户级 JWT（默认租户）。
     *
     * @param user 用户记录
     * @return JWT
     */
    public Jwt issueToken(UserRecord user) {
        return issueToken(user, null);
    }

    private static String normalizeTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return "default";
        }
        return tenantCode.trim();
    }
}
