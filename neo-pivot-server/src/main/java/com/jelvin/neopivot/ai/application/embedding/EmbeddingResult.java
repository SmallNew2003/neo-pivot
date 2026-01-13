package com.jelvin.neopivot.ai.application.embedding;

/**
 * Embedding 结果。
 *
 * @param model 模型标识
 * @param dimension 维度
 * @param vectorLiteral PGVector 可识别的向量字面量（如 [0.1,0.2,...]）
 * @author Jelvin
 */
public record EmbeddingResult(String model, int dimension, String vectorLiteral) {}

