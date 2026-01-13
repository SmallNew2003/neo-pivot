package com.jelvin.neopivot.ai.application.embedding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;

/**
 * 演示用 Embedding：使用 SHA-256 派生固定维度向量。
 *
 * @author Jelvin
 */
@Service
public class MockSha256EmbeddingProvider implements EmbeddingProvider {

    private static final int EMBEDDING_DIMENSION = 32;

    @Override
    public EmbeddingResult embed(String text) {
        String vectorLiteral = embeddingVectorLiteral(text);
        return new EmbeddingResult("mock-embedding-v1", EMBEDDING_DIMENSION, vectorLiteral);
    }

    private static String embeddingVectorLiteral(String text) {
        byte[] digest = sha256Bytes(text == null ? "" : text);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            int b = digest[i % digest.length] & 0xff;
            float v = (b / 255.0f) * 2.0f - 1.0f;
            if (i > 0) {
                sb.append(',');
            }
            sb.append(v);
        }
        sb.append(']');
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
}

