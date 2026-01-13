package com.jelvin.neopivot.storage.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储（S3/MinIO）配置。
 *
 * <p>说明：
 * <ul>
 *   <li>默认 bucket 为 neo-pivot（本地 docker-compose 的 MinIO 初始化同名 bucket）</li>
 *   <li>endpoint 为空表示使用 AWS S3 默认 endpoint（按 region 推导）</li>
 *   <li>endpoint 非空时通常为 MinIO 或自建 S3 兼容服务</li>
 * </ul>
 *
 * @author Jelvin
 */
@ConfigurationProperties(prefix = "neopivot.storage.s3")
@Getter
@Setter
public class StorageS3Properties {

    private String bucket = "neo-pivot";
    private String endpoint;
    private String region = "us-east-1";
    private String accessKey;
    private String secretKey;
    private boolean pathStyleAccess;
    private Duration uploadPresignTtl = Duration.ofMinutes(10);
}
