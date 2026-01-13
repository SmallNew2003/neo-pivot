package com.jelvin.neopivot.storage.application;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * 对象存储读取服务。
 *
 * <p>用于索引链路从 storageUri 读取原始文件内容。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class StorageObjectReadService {

    private final S3Client s3Client;

    /**
     * 读取对象内容为字节数组。
     *
     * @param storageUri 存储定位（当前支持 s3://bucket/key）
     * @return 内容字节
     */
    public byte[] readAllBytes(String storageUri) {
        if (storageUri == null || storageUri.isBlank()) {
            throw new IllegalArgumentException("storageUri 不能为空");
        }

        URI uri = URI.create(storageUri);
        if (!"s3".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("暂不支持的 storageUri scheme: " + uri.getScheme());
        }

        String bucket = uri.getHost();
        String key = uri.getPath();
        if (key != null && key.startsWith("/")) {
            key = key.substring(1);
        }

        if (bucket == null || bucket.isBlank() || key == null || key.isBlank()) {
            throw new IllegalArgumentException("非法 storageUri: " + storageUri);
        }

        GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();
        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("读取对象失败: " + storageUri, e);
        }
    }
}
