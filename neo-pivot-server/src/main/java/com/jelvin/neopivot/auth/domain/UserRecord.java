package com.jelvin.neopivot.auth.domain;

import java.util.List;

/**
 * 用户记录。
 *
 * <p>MVP 阶段使用数据库用户体系（users/user_roles），用于签发用户级 JWT（RS256）。
 *
 * @param id 内部用户 ID（JWT sub）
 * @param username 用户名
 * @param passwordHash 密码哈希（bcrypt/argon2）
 * @param roles 角色列表
 * @author Jelvin
 */
public record UserRecord(Long id, String username, String passwordHash, List<String> roles) {}
