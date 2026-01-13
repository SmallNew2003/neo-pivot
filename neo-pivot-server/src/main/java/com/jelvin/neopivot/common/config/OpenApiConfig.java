package com.jelvin.neopivot.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 *
 * <p>用于 Knife4j / Swagger UI 展示 API 契约，并声明 Bearer JWT 鉴权方式。
 *
 * @author Jelvin
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 配置。
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuth =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info().title("Neo Pivot API").version("v1"))
                .schemaRequirement("bearerAuth", bearerAuth)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

