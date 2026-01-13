package com.jelvin.neopivot.storage.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * storage 模块持久化配置。
 *
 * <p>按模块拆分 Mapper 扫描，避免全局扫描导致模块边界失真。
 *
 * @author Jelvin
 */
@Configuration
@MapperScan("com.jelvin.neopivot.storage.persistence.mapper")
public class StoragePersistenceConfig {}

