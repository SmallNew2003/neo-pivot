package com.jelvin.neopivot.auth.application.login.strategy;

import com.jelvin.neopivot.auth.exception.AuthLoginFailedException;
import com.jelvin.neopivot.auth.domain.LoginGrantType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 登录策略工厂。
 *
 * @author Jelvin
 */
@Component
public class LoginStrategyFactory {

    private final Map<LoginGrantType, LoginStrategy> strategies;

    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        EnumMap<LoginGrantType, LoginStrategy> map = new EnumMap<>(LoginGrantType.class);
        for (LoginStrategy strategy : strategies) {
            if (strategy == null || strategy.grantType() == null) {
                continue;
            }
            if (map.containsKey(strategy.grantType())) {
                throw new IllegalStateException("重复的登录策略: " + strategy.grantType());
            }
            map.put(strategy.grantType(), strategy);
        }
        this.strategies = map;
    }

    /**
     * 按 grantType 获取策略。
     *
     * @param grantType 登录方式
     * @return 策略
     */
    public LoginStrategy get(LoginGrantType grantType) {
        if (grantType == null) {
            throw new AuthLoginFailedException();
        }
        LoginStrategy strategy = strategies.get(grantType);
        if (strategy == null) {
            throw new AuthLoginFailedException();
        }
        return strategy;
    }
}
