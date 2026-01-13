package com.jelvin.neopivot.document.persistence.mapper;

import com.jelvin.neopivot.document.persistence.entity.DocumentEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 文档 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface DocumentMapper extends BaseMapper<DocumentEntity> {

    /**
     * 获取下一个文档 ID（来自 documents_id_seq）。
     *
     * <p>用于 presign 阶段预分配 documentId，以便将 documentId 写入对象 key。
     *
     * @return 下一个文档 ID
     */
    @Select("select nextval('documents_id_seq')")
    Long nextDocumentId();
}
