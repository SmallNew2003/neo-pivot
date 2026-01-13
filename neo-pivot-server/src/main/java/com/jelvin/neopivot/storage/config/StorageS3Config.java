package com.jelvin.neopivot.storage.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3/MinIO 客户端配置。
 *
 * <p>提供 presign 能力（S3Presigner），用于浏览器直传（PUT）与下载（GET）的短期 URL 签发。
 *
 * @author Jelvin
 */
@Configuration
@EnableConfigurationProperties(StorageS3Properties.class)
public class StorageS3Config {

    /**
     * S3 presigner。
     *
     * @param properties 配置
     * @return presigner
     */
    @Bean
    public S3Presigner s3Presigner(StorageS3Properties properties) {
        validateUploadPresignTtl(properties);

        S3Presigner.Builder builder =
                S3Presigner.builder()
                        .region(Region.of(properties.getRegion()))
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                                        .build());

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder = builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        if (properties.getAccessKey() != null
                && !properties.getAccessKey().isBlank()
                && properties.getSecretKey() != null
                && !properties.getSecretKey().isBlank()) {
            builder =
                    builder.credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        } else {
            builder = builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    /**
     * S3 Client（读取对象用）。
     *
     * @param properties 配置
     * @return S3Client
     */
    @Bean
    public S3Client s3Client(StorageS3Properties properties) {
        S3ClientBuilder builder =
                S3Client.builder()
                        .httpClientBuilder(UrlConnectionHttpClient.builder())
                        .region(Region.of(properties.getRegion()))
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                                        .build());

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder = builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        if (properties.getAccessKey() != null
                && !properties.getAccessKey().isBlank()
                && properties.getSecretKey() != null
                && !properties.getSecretKey().isBlank()) {
            builder =
                    builder.credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        } else {
            builder = builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    private static void validateUploadPresignTtl(StorageS3Properties properties) {
        if (properties.getUploadPresignTtl() == null) {
            throw new IllegalArgumentException("neopivot.storage.s3.upload-presign-ttl 不能为空");
        }
        if (properties.getUploadPresignTtl().isNegative() || properties.getUploadPresignTtl().isZero()) {
            throw new IllegalArgumentException("neopivot.storage.s3.upload-presign-ttl 必须为正数");
        }
        if (properties.getUploadPresignTtl().compareTo(Duration.ofMinutes(10)) > 0) {
            throw new IllegalArgumentException("上传 presign TTL 强约束为不超过 10 分钟");
        }
    }
}
