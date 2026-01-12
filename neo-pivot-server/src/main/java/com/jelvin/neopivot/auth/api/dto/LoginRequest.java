package com.jelvin.neopivot.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 标准登录请求体。
 *
 * <p>用于管理台或平台入口（方案A）获取用户级 JWT。
 *
 * @author Jelvin
 */
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
