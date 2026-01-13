package com.jelvin.neopivot.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 认证与 JWT 签发相关配置。
 *
 * <p>说明：
 * <ul>
 *   <li>MVP 阶段用户体系采用数据库表（users/user_roles），并通过 Liquibase 初始化。</li>
 *   <li>生产环境可替换为企业 IdP，但 JWT 语义（iss/aud/sub/roles）应保持一致。</li>
 *   <li>JWT 使用 RS256 签名，服务同时提供 JWKS 公钥发布端点供资源服务验签。</li>
 * </ul>
 *
 * @author Jelvin
 */
@Validated
@ConfigurationProperties(prefix = "neopivot.auth")
@Getter
@Setter
public class AuthProperties {

    @NotBlank
    private String issuer;

    @NotBlank
    private String audience;

    @NotNull
    private Duration tokenTtl = Duration.ofHours(1);
}
