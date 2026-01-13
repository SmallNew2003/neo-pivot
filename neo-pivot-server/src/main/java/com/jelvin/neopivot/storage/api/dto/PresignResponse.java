package com.jelvin.neopivot.storage.api.dto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * presigned 上传凭证响应体。
 *
 * 主路径为 presigned PUT：前端拿到 URL 后直接 PUT 文件内容到对象存储。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class PresignResponse {

    private String presignId;
    private String documentId;
    private String storageUri;
    private String uploadMethod;
    private String uploadUrl;
    private Map<String, String> headers = new HashMap<>();
    private Instant expiresAt;
}
