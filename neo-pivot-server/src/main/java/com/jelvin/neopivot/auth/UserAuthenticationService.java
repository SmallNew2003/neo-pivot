package com.jelvin.neopivot.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务（演示用途）。
 *
 * <p>基于配置内置用户列表做用户名/密码校验，用于签发用户级 JWT。
 *
 * @author Jelvin
 */
@Service
public class UserAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, UserRecord> usersByUsername;

    /**
     * 构造函数。
     *
     * @param passwordEncoder 密码编码器（bcrypt）
     * @param authProperties 认证配置
     */
    public UserAuthenticationService(PasswordEncoder passwordEncoder, AuthProperties authProperties) {
        this.passwordEncoder = passwordEncoder;
        this.usersByUsername = new HashMap<>();
        for (AuthProperties.UserDefinition user : authProperties.getUsers()) {
            String id = user.getUsername();
            usersByUsername.put(
                    user.getUsername(),
                    new UserRecord(id, user.getUsername(), user.getPasswordBcrypt(), user.getRoles()));
        }
    }

    /**
     * 校验用户名与密码是否正确。
     *
     * @param username 用户名
     * @param rawPassword 明文密码
     * @return 校验成功则返回用户记录
     */
    public Optional<UserRecord> authenticate(String username, String rawPassword) {
        UserRecord user = usersByUsername.get(username);
        if (user == null) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(rawPassword, user.passwordBcrypt())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}

