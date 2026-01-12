package com.jelvin.neopivot.auth.persistence.mapper;

import com.jelvin.neopivot.auth.persistence.entity.UserRoleEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {}

