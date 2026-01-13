package com.jelvin.neopivot.ai.application.retrieval;

import com.jelvin.neopivot.ai.application.embedding.EmbeddingResult;
import com.jelvin.neopivot.ai.persistence.dto.ChunkSearchHit;
import com.jelvin.neopivot.ai.persistence.mapper.DocumentChunkEmbeddingMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * PGVector 向量召回实现（Top-K）。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class PgVectorChunkRetriever implements ChunkRetriever {

    private final DocumentChunkEmbeddingMapper documentChunkEmbeddingMapper;

    @Override
    public List<ChunkSearchHit> retrieveTopK(long ownerId, EmbeddingResult queryEmbedding, int topK) {
        if (queryEmbedding == null) {
            return List.of();
        }
        return documentChunkEmbeddingMapper.searchTopK(ownerId, queryEmbedding.model(), queryEmbedding.vectorLiteral(), topK);
    }
}
