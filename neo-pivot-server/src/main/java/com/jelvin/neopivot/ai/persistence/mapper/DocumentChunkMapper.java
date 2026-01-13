package com.jelvin.neopivot.ai.persistence.mapper;

import com.jelvin.neopivot.ai.persistence.entity.DocumentChunkEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档分块 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunkEntity> {

    /**
     * 删除指定文档的全部分块（幂等重建索引用）。
     *
     * @param documentId 文档 ID
     * @return 删除行数
     */
    @Delete("delete from document_chunks where document_id = #{documentId}")
    int deleteByDocumentId(Long documentId);
}

