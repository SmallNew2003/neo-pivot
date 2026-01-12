package com.jelvin.neopivot.chat.api.dto;

/**
 * 引用片段（citation）。
 *
 * <p>用于解释答案来源，方便审计与调试。
 *
 * @author Jelvin
 */
public class CitationDto {

    private String documentId;
    private String chunkId;
    private Integer chunkIndex;
    private String contentSnippet;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContentSnippet() {
        return contentSnippet;
    }

    public void setContentSnippet(String contentSnippet) {
        this.contentSnippet = contentSnippet;
    }
}
