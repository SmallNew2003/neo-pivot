package com.jelvin.neopivot.ai.application.retrieval;

import com.jelvin.neopivot.ai.application.embedding.EmbeddingResult;
import com.jelvin.neopivot.ai.persistence.dto.ChunkSearchHit;
import java.util.List;

/**
 * Chunk 召回器抽象。
 *
 * @author Jelvin
 */
public interface ChunkRetriever {

    List<ChunkSearchHit> retrieveTopK(long ownerId, EmbeddingResult queryEmbedding, int topK);
}

