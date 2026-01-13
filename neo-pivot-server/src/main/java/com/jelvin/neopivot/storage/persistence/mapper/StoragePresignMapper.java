package com.jelvin.neopivot.storage.persistence.mapper;

import com.jelvin.neopivot.storage.persistence.entity.StoragePresignEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对象存储预签名记录 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface StoragePresignMapper extends BaseMapper<StoragePresignEntity> {}

