package com.jelvin.neopivot.auth.application.user;

import com.jelvin.neopivot.auth.persistence.entity.UserEntity;
import com.jelvin.neopivot.auth.persistence.entity.UserRoleEntity;
import com.jelvin.neopivot.auth.persistence.mapper.UserMapper;
import com.jelvin.neopivot.auth.persistence.mapper.UserRoleMapper;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户创建与初始化服务。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 创建用户（自动生成随机密码哈希）并赋予默认角色。
     *
     * @param username 用户名
     * @return 新用户 ID
     */
    public Long createUser(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username 不能为空");
        }

        Instant now = Instant.now();
        UserEntity entity = new UserEntity();
        entity.setUsername(username.trim());
        entity.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        userMapper.insert(entity);

        UserRoleEntity roleEntity = new UserRoleEntity();
        roleEntity.setUserId(entity.getId());
        roleEntity.setRole("USER");
        userRoleMapper.insert(roleEntity);

        return entity.getId();
    }
}
