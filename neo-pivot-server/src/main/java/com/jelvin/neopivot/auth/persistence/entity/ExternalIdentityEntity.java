package com.jelvin.neopivot.auth.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 外部身份映射实体（external_identities）。
 *
 * @author Jelvin
 */
@Table(value = "external_identities", camelToUnderline = true)
@Getter
@Setter
public class ExternalIdentityEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String tenantCode;

    private String provider;

    private String externalSubject;

    private Long userId;

    private Instant createdAt;
}

