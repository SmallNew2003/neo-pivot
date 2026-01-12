package com.jelvin.neopivot.auth;

import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关 API。
 *
 * <p>提供标准登录接口，用于获取用户级 JWT，支撑：
 * <ul>
 *   <li>前端管理台标准登录</li>
 *   <li>平台接入方案A（透传终端用户 JWT）</li>
 * </ul>
 *
 * @author Jelvin
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthProperties authProperties;
    private final UserAuthenticationService userAuthenticationService;
    private final JwtTokenService jwtTokenService;

    /**
     * 构造函数。
     *
     * @param authProperties 配置
     * @param userAuthenticationService 用户认证服务
     * @param jwtTokenService JWT 签发服务
     */
    public AuthController(
            AuthProperties authProperties,
            UserAuthenticationService userAuthenticationService,
            JwtTokenService jwtTokenService) {
        this.authProperties = authProperties;
        this.userAuthenticationService = userAuthenticationService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 标准登录：用户名/密码换取 JWT。
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Optional<UserRecord> user = userAuthenticationService.authenticate(request.getUsername(), request.getPassword());
        if (user.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        var jwt = jwtTokenService.issueToken(user.get());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(jwt.getTokenValue());
        response.setTokenType("Bearer");
        response.setExpiresInSeconds(authProperties.getTokenTtl().toSeconds());

        LoginResponse.UserView userView = new LoginResponse.UserView();
        userView.setId(user.get().id());
        userView.setUsername(user.get().username());
        userView.setRoles(user.get().roles());
        response.setUser(userView);

        return response;
    }

    /**
     * 登录失败异常：统一返回 401。
     *
     * @author Jelvin
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    private static class InvalidCredentialsException extends RuntimeException {}
}

