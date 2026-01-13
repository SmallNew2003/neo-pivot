package com.jelvin.neopivot.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 标准登录请求体。
 *
 * <p>用于管理台或平台入口（方案A）获取用户级 JWT。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
