package com.jelvin.neopivot.storage.config;

import java.time.Duration;
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
public class StorageS3Properties {

    private String bucket = "neo-pivot";
    private String endpoint;
    private String region = "us-east-1";
    private String accessKey;
    private String secretKey;
    private boolean pathStyleAccess;
    private Duration uploadPresignTtl = Duration.ofMinutes(10);

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public Duration getUploadPresignTtl() {
        return uploadPresignTtl;
    }

    public void setUploadPresignTtl(Duration uploadPresignTtl) {
        this.uploadPresignTtl = uploadPresignTtl;
    }
}

