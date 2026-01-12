package com.jelvin.neopivot.auth.api.dto;

import java.util.List;

/**
 * 标准登录响应体。
 *
 * <p>与 OpenSpec `openspec/specs/0003-auth-login-spec.md` 对齐。
 *
 * @author Jelvin
 */
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private Long expiresInSeconds;

    private UserView user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public UserView getUser() {
        return user;
    }

    public void setUser(UserView user) {
        this.user = user;
    }

    /**
     * 用户信息视图（用于前端展示与调试）。
     *
     * @author Jelvin
     */
    public static class UserView {

        private String id;
        private String username;
        private List<String> roles;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
