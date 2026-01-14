package com.jelvin.neopivot.auth.application.user;

import com.jelvin.neopivot.auth.domain.UserRecord;
import com.jelvin.neopivot.auth.persistence.entity.UserEntity;
import com.jelvin.neopivot.auth.persistence.entity.UserRoleEntity;
import com.jelvin.neopivot.auth.persistence.mapper.UserMapper;
import com.jelvin.neopivot.auth.persistence.mapper.UserRoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户查询服务。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 按用户 ID 获取用户记录（含角色）。
     *
     * @param userId 用户 ID
     * @return 用户记录
     */
    public Optional<UserRecord> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        UserEntity user = userMapper.selectOneById(userId);
        if (user == null) {
            return Optional.empty();
        }

        QueryWrapper rolesQuery = QueryWrapper.create().where("user_id = ?", user.getId());
        List<String> roles =
                userRoleMapper.selectListByQuery(rolesQuery).stream()
                        .map(UserRoleEntity::getRole)
                        .collect(Collectors.toList());

        return Optional.of(new UserRecord(user.getId(), user.getUsername(), user.getPasswordHash(), roles));
    }
}
