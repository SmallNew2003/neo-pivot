package com.jelvin.neopivot.auth.application.login.strategy;

import com.jelvin.neopivot.auth.api.dto.LoginRequest;
import com.jelvin.neopivot.auth.application.login.LoginAuthResult;
import com.jelvin.neopivot.auth.application.login.UserAuthenticationService;
import com.jelvin.neopivot.auth.domain.LoginGrantType;
import com.jelvin.neopivot.auth.domain.UserRecord;
import com.jelvin.neopivot.auth.exception.AuthLoginFailedException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户名/密码登录策略。
 *
 * @author Jelvin
 */
@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final UserAuthenticationService userAuthenticationService;

    @Override
    public LoginGrantType grantType() {
        return LoginGrantType.PASSWORD;
    }

    @Override
    public LoginAuthResult authenticate(String tenantCode, LoginRequest request) {
        LoginRequest.PasswordGrant payload = request == null ? null : request.getPassword();
        String username = payload == null ? null : payload.getUsername();
        String password = payload == null ? null : payload.getPassword();

        Optional<UserRecord> user = userAuthenticationService.authenticate(username, password);
        if (user.isEmpty()) {
            throw new AuthLoginFailedException();
        }
        return new LoginAuthResult(user.get(), null, username);
    }
}
