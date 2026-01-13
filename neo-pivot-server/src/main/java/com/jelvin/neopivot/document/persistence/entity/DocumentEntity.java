package com.jelvin.neopivot.document.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 文档实体（documents）。
 *
 * @author Jelvin
 */
@Table(value = "documents", camelToUnderline = true)
@Getter
@Setter
public class DocumentEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long ownerId;
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private String sha256;
    private String storageUri;
    private String status;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
