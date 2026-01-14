package com.jelvin.neopivot.auth.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录行为日志实体（login_audit_logs）。
 *
 * @author Jelvin
 */
@Table(value = "login_audit_logs", camelToUnderline = true)
@Getter
@Setter
public class LoginAuditLogEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String tenantCode;

    private String grantType;

    private String provider;

    private Long userId;

    private Boolean success;

    private String failureReason;

    private String target;

    private String traceId;

    private String ip;

    private String userAgent;

    private Instant createdAt;
}

