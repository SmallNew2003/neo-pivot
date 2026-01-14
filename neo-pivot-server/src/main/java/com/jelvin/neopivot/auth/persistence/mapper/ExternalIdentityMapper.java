package com.jelvin.neopivot.auth.persistence.mapper;

import com.jelvin.neopivot.auth.persistence.entity.ExternalIdentityEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 外部身份映射 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface ExternalIdentityMapper extends BaseMapper<ExternalIdentityEntity> {}

