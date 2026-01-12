package com.jelvin.neopivot.auth.persistence.mapper;

import com.jelvin.neopivot.auth.persistence.entity.UserEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {}

