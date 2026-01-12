package com.jelvin.neopivot.auth.persistence.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体（users）。
 *
 * @author Jelvin
 */
@Table(value = "users", camelToUnderline = true)
@Getter
@Setter
public class UserEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String username;

    private String passwordHash;

    private Instant createdAt;

    private Instant updatedAt;
}
