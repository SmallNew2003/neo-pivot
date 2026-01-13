package com.jelvin.neopivot.storage.application;

import com.jelvin.neopivot.document.persistence.mapper.DocumentMapper;
import org.springframework.stereotype.Service;

/**
 * 文档 ID 预分配服务。
 *
 * <p>用于 presign 阶段预分配 documentId，以便按 OpenSpec 0016 将 documentId 写入对象 key：
 * s3://neo-pivot/&lt;userId&gt;/&lt;documentId&gt;/&lt;filename&gt;。
 *
 * @author Jelvin
 */
@Service
public class DocumentIdAllocatorService {

    private final DocumentMapper documentMapper;

    /**
     * 构造函数。
     *
     * @param documentMapper 文档 Mapper
     */
    public DocumentIdAllocatorService(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    /**
     * 分配下一个文档 ID。
     *
     * @return 文档 ID
     */
    public long nextDocumentId() {
        Long value = documentMapper.nextDocumentId();
        if (value == null) {
            throw new IllegalStateException("无法从 documents_id_seq 获取下一个文档 ID");
        }
        return value;
    }
}
