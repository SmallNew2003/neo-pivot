package com.jelvin.neopivot.storage.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * presigned 上传凭证请求体。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class PresignRequest {

    @NotBlank
    private String filename;

    @NotBlank
    private String contentType;

    @Positive
    private Long sizeBytes;

    private String sha256;
}
