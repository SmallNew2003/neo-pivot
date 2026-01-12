package com.jelvin.neopivot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Neo Pivot 后端应用入口。
 *
 * <p>该服务作为“核心底座”对外提供 API，并承担：
 * <ul>
 *   <li>用户认证/鉴权（JWT RS256，方案A：透传终端用户 JWT）</li>
 *   <li>RAG 问答主链路（模式A：最终答案生成由底座统一负责）</li>
 *   <li>对象存储与向量检索相关能力（后续实现阶段补齐）</li>
 * </ul>
 *
 * @author Jelvin
 */
@SpringBootApplication
public class NeoPivotApplication {

    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NeoPivotApplication.class, args);
    }
}

