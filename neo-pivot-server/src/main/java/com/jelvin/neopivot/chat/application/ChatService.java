package com.jelvin.neopivot.chat.application;

import com.jelvin.neopivot.ai.application.embedding.EmbeddingProvider;
import com.jelvin.neopivot.ai.application.embedding.EmbeddingResult;
import com.jelvin.neopivot.ai.application.retrieval.ChunkRetriever;
import com.jelvin.neopivot.ai.persistence.dto.ChunkSearchHit;
import com.jelvin.neopivot.chat.api.dto.ChatRequest;
import com.jelvin.neopivot.chat.api.dto.ChatResponse;
import com.jelvin.neopivot.chat.api.dto.CitationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 问答应用服务（MVP：先实现检索与 citations 闭环）。
 *
 * <p>当前实现采用“最小可用”的检索闭环：
 * <ul>
 *   <li>对 question 生成 embedding（mock）</li>
 *   <li>在 PGVector 中做 Top-K 相似度检索（owner 过滤）</li>
 *   <li>返回 citations，并生成一个可解释的占位答案</li>
 * </ul>
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final EmbeddingProvider embeddingProvider;
    private final ChunkRetriever chunkRetriever;

    /**
     * 执行问答（检索 + citations）。
     *
     * @param ownerId 当前用户 ID（JWT sub）
     * @param request 请求体
     * @return 响应体
     */
    public ChatResponse chat(long ownerId, ChatRequest request) {
        int topK = normalizeTopK(request == null ? null : request.getTopK());
        EmbeddingResult queryEmbedding = embeddingProvider.embed(request == null ? null : request.getQuestion());

        List<ChunkSearchHit> hits = chunkRetriever.retrieveTopK(ownerId, queryEmbedding, topK);
        List<CitationDto> citations =
                hits.stream()
                        .map(hit -> {
                            CitationDto dto = new CitationDto();
                            dto.setChunkId(hit.getChunkId() == null ? null : String.valueOf(hit.getChunkId()));
                            dto.setDocumentId(hit.getDocumentId() == null ? null : String.valueOf(hit.getDocumentId()));
                            dto.setChunkIndex(hit.getChunkIndex());
                            dto.setContentSnippet(truncate(hit.getContent(), 220));
                            return dto;
                        })
                        .toList();

        ChatResponse response = new ChatResponse();
        response.setCitations(citations);
        response.setAnswer(buildAnswer(request == null ? null : request.getQuestion(), citations));
        return response;
    }

    private static int normalizeTopK(Integer topK) {
        if (topK == null) {
            return 5;
        }
        if (topK < 1) {
            return 1;
        }
        if (topK > 20) {
            return 20;
        }
        return topK;
    }

    private static String buildAnswer(String question, List<CitationDto> citations) {
        if (citations == null || citations.isEmpty()) {
            return "未检索到相关片段。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("基于检索到的片段（Top ").append(citations.size()).append("）给出回答占位：\n");
        if (question != null && !question.isBlank()) {
            sb.append("问题：").append(truncate(question.trim(), 120)).append("\n\n");
        }
        for (int i = 0; i < citations.size(); i++) {
            CitationDto c = citations.get(i);
            sb.append(i + 1)
                    .append(". ")
                    .append(c.getContentSnippet() == null ? "" : c.getContentSnippet())
                    .append("\n");
        }
        return sb.toString().trim();
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }
}
