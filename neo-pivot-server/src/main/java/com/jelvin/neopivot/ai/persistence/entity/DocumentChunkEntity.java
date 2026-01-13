package com.jelvin.neopivot.ai.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 文档分块实体（document_chunks）。
 *
 * @author Jelvin
 */
@Table(value = "document_chunks", camelToUnderline = true)
@Getter
@Setter
public class DocumentChunkEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long documentId;
    private Long ownerId;
    private Integer chunkIndex;
    private String content;
    private String contentHash;
    private Instant createdAt;
}

