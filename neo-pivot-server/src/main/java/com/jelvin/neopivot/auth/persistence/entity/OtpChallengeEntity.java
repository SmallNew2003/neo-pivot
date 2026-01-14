package com.jelvin.neopivot.auth.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * OTP challenge 实体（otp_challenges）。
 *
 * @author Jelvin
 */
@Table(value = "otp_challenges", camelToUnderline = true)
@Getter
@Setter
public class OtpChallengeEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String tenantCode;

    private String channel;

    private String target;

    private String challengeId;

    private String codeHash;

    private Instant expiresAt;

    private Instant usedAt;

    private Integer verifyAttempts;

    private Instant lastSentAt;

    private Instant createdAt;

    private Instant updatedAt;
}

