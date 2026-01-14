package com.jelvin.neopivot.auth.application.login;

import com.jelvin.neopivot.auth.domain.ExternalIdentityProvider;
import com.jelvin.neopivot.auth.domain.UserRecord;

/**
 * 登录认证结果。
 *
 * @param user 用户记录
 * @param provider 外部身份提供方（可空）
 * @param target 登录目标标识（用户名/手机号/邮箱/外部 subject）
 * @author Jelvin
 */
public record LoginAuthResult(UserRecord user, ExternalIdentityProvider provider, String target) {}
