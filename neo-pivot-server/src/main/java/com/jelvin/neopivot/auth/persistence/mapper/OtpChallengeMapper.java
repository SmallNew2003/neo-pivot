package com.jelvin.neopivot.auth.persistence.mapper;

import com.jelvin.neopivot.auth.persistence.entity.OtpChallengeEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * OTP challenge Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface OtpChallengeMapper extends BaseMapper<OtpChallengeEntity> {}

