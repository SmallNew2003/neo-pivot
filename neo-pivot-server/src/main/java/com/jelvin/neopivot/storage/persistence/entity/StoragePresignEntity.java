package com.jelvin.neopivot.storage.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 对象存储预签名记录实体（storage_presigns）。
 *
 * @author Jelvin
 */
@Table(value = "storage_presigns", camelToUnderline = true)
@Getter
@Setter
public class StoragePresignEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long ownerId;
    private Long documentId;
    private String purpose;
    private String method;
    private String storageUri;
    private String status;
    private Instant expiresAt;
    private String issuedIp;
    private String issuedUserAgent;
    private Instant consumedAt;
    private String consumedIp;
    private Instant createdAt;
}
