package com.jelvin.neopivot.ai.application;

import com.jelvin.neopivot.ai.application.embedding.EmbeddingProvider;
import com.jelvin.neopivot.ai.application.embedding.EmbeddingResult;
import com.jelvin.neopivot.ai.persistence.entity.DocumentChunkEntity;
import com.jelvin.neopivot.ai.persistence.mapper.DocumentChunkEmbeddingMapper;
import com.jelvin.neopivot.ai.persistence.mapper.DocumentChunkMapper;
import com.jelvin.neopivot.common.events.DocumentUploadedEvent;
import com.jelvin.neopivot.document.persistence.entity.DocumentEntity;
import com.jelvin.neopivot.document.persistence.mapper.DocumentMapper;
import com.jelvin.neopivot.storage.application.StorageObjectReadService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 文档索引服务（上传后异步索引）。
 *
 * <p>职责：
 * <ul>
 *   <li>监听 {@link DocumentUploadedEvent}（AFTER_COMMIT），读取对象内容</li>
 *   <li>解析为纯文本并分块，写入 document_chunks</li>
 *   <li>生成 embedding 并写入 document_chunk_embeddings</li>
 *   <li>更新 documents 状态机（INDEXING/INDEXED/FAILED）</li>
 * </ul>
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class DocumentIndexingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndexingService.class);

    private static final int CHUNK_SIZE_CHARS = 800;
    private static final int CHUNK_OVERLAP_CHARS = 100;

    private final DocumentMapper documentMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final DocumentChunkEmbeddingMapper documentChunkEmbeddingMapper;
    private final StorageObjectReadService storageObjectReadService;
    private final EmbeddingProvider embeddingProvider;

    /**
     * 处理文档上传完成事件（事务提交后）。
     *
     * @param event 上传完成事件
     */
    @Async
    @TransactionalEventListener
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        if (event == null || event.documentId() == null) {
            return;
        }
        try {
            indexDocument(event.documentId());
        } catch (Exception e) {
            log.error("Indexing failed: documentId={}", event.documentId(), e);
        }
    }

    /**
     * 为指定文档执行索引（可重入/幂等：会先清理旧分块数据）。
     *
     * @param documentId 文档 ID
     */
    @Transactional
    public void indexDocument(Long documentId) {
        DocumentEntity document = documentMapper.selectOneById(documentId);
        if (document == null) {
            return;
        }

        Instant now = Instant.now();
        markDocumentStatus(document, "INDEXING", null, now);

        try {
            if (!isTextIndexable(document.getContentType(), document.getFilename())) {
                throw new UnsupportedOperationException("暂不支持的文档类型: " + document.getContentType());
            }

            byte[] bytes = storageObjectReadService.readAllBytes(document.getStorageUri());
            String text = new String(bytes, StandardCharsets.UTF_8);
            String normalizedText = normalizeText(text);

            documentChunkMapper.deleteByDocumentId(documentId);

            List<String> chunks = chunkText(normalizedText);
            int indexedChunkCount = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String chunkContent = chunks.get(i);
                if (chunkContent == null || chunkContent.isBlank()) {
                    continue;
                }

                DocumentChunkEntity chunkEntity = new DocumentChunkEntity();
                chunkEntity.setDocumentId(documentId);
                chunkEntity.setOwnerId(document.getOwnerId());
                chunkEntity.setChunkIndex(indexedChunkCount);
                chunkEntity.setContent(chunkContent);
                chunkEntity.setContentHash(sha256Hex(chunkContent));
                chunkEntity.setCreatedAt(now);
                documentChunkMapper.insertSelective(chunkEntity);

                EmbeddingResult embedding = embeddingProvider.embed(chunkContent);
                documentChunkEmbeddingMapper.insertEmbedding(
                        chunkEntity.getId(), document.getOwnerId(), embedding.vectorLiteral(), embedding.model(), now);
                indexedChunkCount++;
            }

            markDocumentStatus(document, "INDEXED", null, now);
            log.info("Indexing done: documentId={}, chunks={}", documentId, indexedChunkCount);
        } catch (Exception e) {
            String errorMessage = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            markDocumentStatus(document, "FAILED", truncate(errorMessage, 500), now);
            log.warn("Indexing failed: documentId={}, error={}", documentId, errorMessage);
        }
    }

    private void markDocumentStatus(DocumentEntity document, String status, String errorMessage, Instant now) {
        document.setStatus(status);
        document.setErrorMessage(errorMessage);
        document.setUpdatedAt(now);
        documentMapper.update(document);
    }

    private static boolean isTextIndexable(String contentType, String filename) {
        if (contentType != null) {
            String ct = contentType.toLowerCase();
            if (ct.startsWith("text/")) {
                return true;
            }
            if ("application/json".equals(ct) || "application/xml".equals(ct)) {
                return true;
            }
        }
        if (filename != null) {
            String lower = filename.toLowerCase();
            return lower.endsWith(".txt") || lower.endsWith(".md");
        }
        return false;
    }

    private static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private static List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int size = text.length();
        int step = Math.max(1, CHUNK_SIZE_CHARS - CHUNK_OVERLAP_CHARS);
        for (int start = 0; start < size; start += step) {
            int end = Math.min(start + CHUNK_SIZE_CHARS, size);
            String chunk = text.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }
            if (end == size) {
                break;
            }
        }
        return chunks;
    }

    private static String sha256Hex(String text) {
        byte[] digest = sha256Bytes(text == null ? "" : text);
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static byte[] sha256Bytes(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
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
