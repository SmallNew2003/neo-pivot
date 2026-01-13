package com.jelvin.neopivot.ai.application.embedding;

/**
 * Embedding 生成器抽象。
 *
 * @author Jelvin
 */
public interface EmbeddingProvider {

    EmbeddingResult embed(String text);
}

