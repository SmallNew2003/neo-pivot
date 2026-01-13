package com.jelvin.neopivot.document.api.dto;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 文档 DTO。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class DocumentDto {

    private String id;
    private String filename;
    private String storageUri;
    private String status;
    private String errorMessage;
    private Instant createdAt;
}
