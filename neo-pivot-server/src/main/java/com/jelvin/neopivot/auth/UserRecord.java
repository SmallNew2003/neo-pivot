package com.jelvin.neopivot.auth;

import java.util.List;

/**
 * 内存用户记录（演示用途）。
 *
 * <p>MVP 阶段使用配置驱动的用户列表，用于跑通“标准登录 + JWT RS256”链路。
 * 后续可替换为数据库用户体系或接入企业 IdP。
 *
 * @param id 用户唯一标识（JWT sub）
 * @param username 用户名
 * @param passwordBcrypt bcrypt 密码哈希
 * @param roles 角色列表
 * @author Jelvin
 */
public record UserRecord(String id, String username, String passwordBcrypt, List<String> roles) {}

