package com.jelvin.neopivot.chat.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 引用片段（citation）。
 *
 * <p>用于解释答案来源，方便审计与调试。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class CitationDto {

    private String documentId;
    private String chunkId;
    private Integer chunkIndex;
    private String contentSnippet;
}
