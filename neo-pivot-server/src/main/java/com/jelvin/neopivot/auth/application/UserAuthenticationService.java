package com.jelvin.neopivot.auth.application;

import com.jelvin.neopivot.auth.domain.UserRecord;
import com.jelvin.neopivot.auth.persistence.entity.UserEntity;
import com.jelvin.neopivot.auth.persistence.entity.UserRoleEntity;
import com.jelvin.neopivot.auth.persistence.mapper.UserMapper;
import com.jelvin.neopivot.auth.persistence.mapper.UserRoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务。
 *
 * <p>基于数据库用户表做用户名/密码校验，用于签发用户级 JWT。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 校验用户名与密码是否正确。
     *
     * @param username 用户名
     * @param rawPassword 明文密码
     * @return 校验成功则返回用户记录
     */
    public Optional<UserRecord> authenticate(String username, String rawPassword) {
        QueryWrapper userQuery = QueryWrapper.create().where("username = ?", username);
        UserEntity user = userMapper.selectOneByQuery(userQuery);
        if (user == null) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return Optional.empty();
        }

        QueryWrapper rolesQuery = QueryWrapper.create().where("user_id = ?", user.getId());
        var roles =
                userRoleMapper.selectListByQuery(rolesQuery).stream()
                        .map(UserRoleEntity::getRole)
                        .collect(Collectors.toList());

        return Optional.of(new UserRecord(user.getId(), user.getUsername(), user.getPasswordHash(), roles));
    }
}
