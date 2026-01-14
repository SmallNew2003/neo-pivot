package com.jelvin.neopivot.auth.application.login.strategy;

import com.jelvin.neopivot.auth.api.dto.LoginRequest;
import com.jelvin.neopivot.auth.application.login.LoginAuthResult;
import com.jelvin.neopivot.auth.domain.LoginGrantType;

/**
 * 登录策略（按 grantType 扩展）。
 *
 * @author Jelvin
 */
public interface LoginStrategy {

    /**
     * 支持的登录方式。
     *
     * @return grantType
     */
    LoginGrantType grantType();

    /**
     * 执行认证并解析内部用户。
     *
     * @param tenantCode 租户标识
     * @param request 请求体
     * @return 认证结果
     */
    LoginAuthResult authenticate(String tenantCode, LoginRequest request);
}
