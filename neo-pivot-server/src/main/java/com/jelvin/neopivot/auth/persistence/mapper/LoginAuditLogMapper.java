package com.jelvin.neopivot.auth.persistence.mapper;

import com.jelvin.neopivot.auth.persistence.entity.LoginAuditLogEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录行为日志 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface LoginAuditLogMapper extends BaseMapper<LoginAuditLogEntity> {}

