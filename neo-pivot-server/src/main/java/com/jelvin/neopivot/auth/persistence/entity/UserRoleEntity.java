package com.jelvin.neopivot.auth.persistence.entity;

import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户角色实体（user_roles）。
 *
 * @author Jelvin
 */
@Table(value = "user_roles", camelToUnderline = true)
@Getter
@Setter
public class UserRoleEntity {

    private Long userId;

    private String role;
}
