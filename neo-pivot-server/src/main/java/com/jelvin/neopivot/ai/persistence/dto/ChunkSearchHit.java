package com.jelvin.neopivot.ai.persistence.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 向量检索命中结果（用于 Chat citations）。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class ChunkSearchHit {

    private Long chunkId;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
}

