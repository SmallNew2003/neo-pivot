package com.jelvin.neopivot.document.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建文档请求体（S3 直传后回调落库）。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class CreateDocumentRequest {

    @NotBlank
    private String presignId;

    @NotBlank
    private String documentId;

    @NotBlank
    private String storageUri;

    @NotBlank
    private String filename;

    @NotBlank
    private String contentType;

    @Positive
    private Long sizeBytes;

    private String sha256;
}
