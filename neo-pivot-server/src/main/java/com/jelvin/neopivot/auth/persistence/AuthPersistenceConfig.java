package com.jelvin.neopivot.auth.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 认证模块持久层配置。
 *
 * <p>按模块拆分 Mapper 扫描，避免全局扫描导致模块边界失真。
 *
 * @author Jelvin
 */
@Configuration
@MapperScan("com.jelvin.neopivot.auth.persistence.mapper")
public class AuthPersistenceConfig {}

