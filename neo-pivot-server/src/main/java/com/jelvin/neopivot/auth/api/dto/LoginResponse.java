package com.jelvin.neopivot.auth.api.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 标准登录响应体。
 *
 * <p>与 OpenSpec `openspec/specs/0003-auth-login-spec.md` 对齐。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private Long expiresInSeconds;

    private UserView user;

    /**
     * 用户信息视图（用于前端展示与调试）。
     *
     * @author Jelvin
     */
    @Getter
    @Setter
    public static class UserView {

        private String id;
        private String username;
        private List<String> roles;
    }
}
