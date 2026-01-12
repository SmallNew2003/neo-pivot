package com.jelvin.neopivot.api.dto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * presigned 上传凭证响应体。
 *
 * <p>主路径为 presigned PUT：前端拿到 URL 后直接 PUT 文件内容到对象存储。
 *
 * @author Jelvin
 */
public class PresignResponse {

    private String storageUri;
    private String uploadMethod;
    private String uploadUrl;
    private Map<String, String> headers = new HashMap<>();
    private Instant expiresAt;

    public String getStorageUri() {
        return storageUri;
    }

    public void setStorageUri(String storageUri) {
        this.storageUri = storageUri;
    }

    public String getUploadMethod() {
        return uploadMethod;
    }

    public void setUploadMethod(String uploadMethod) {
        this.uploadMethod = uploadMethod;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}

