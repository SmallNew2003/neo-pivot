package com.jelvin.neopivot.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 认证与 JWT 签发相关配置。
 *
 * <p>说明：
 * <ul>
 *   <li>MVP 阶段使用配置内置用户（演示用途）。</li>
 *   <li>生产环境应替换为数据库用户体系或接入企业 IdP。</li>
 *   <li>JWT 使用 RS256 签名，服务同时提供 JWKS 公钥发布端点供资源服务验签。</li>
 * </ul>
 *
 * @author Jelvin
 */
@Validated
@ConfigurationProperties(prefix = "neopivot.auth")
public class AuthProperties {

    @NotBlank
    private String issuer;

    @NotBlank
    private String audience;

    @NotNull
    private Duration tokenTtl = Duration.ofHours(1);

    @NotNull
    @Valid
    private List<UserDefinition> users = new ArrayList<>();

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public List<UserDefinition> getUsers() {
        return users;
    }

    public void setUsers(List<UserDefinition> users) {
        this.users = users;
    }

    /**
     * 内置用户定义（演示用途）。
     *
     * @author Jelvin
     */
    public static class UserDefinition {

        @NotBlank
        private String username;

        /**
         * bcrypt 哈希后的密码字符串。
         */
        @NotBlank
        private String passwordBcrypt;

        @NotNull
        private List<String> roles = new ArrayList<>();

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPasswordBcrypt() {
            return passwordBcrypt;
        }

        public void setPasswordBcrypt(String passwordBcrypt) {
            this.passwordBcrypt = passwordBcrypt;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}

