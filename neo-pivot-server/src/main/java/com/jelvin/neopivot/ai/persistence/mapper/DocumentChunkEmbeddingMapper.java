package com.jelvin.neopivot.ai.persistence.mapper;

import com.jelvin.neopivot.ai.persistence.dto.ChunkSearchHit;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文档分块向量 Mapper。
 *
 * @author Jelvin
 */
@Mapper
public interface DocumentChunkEmbeddingMapper {

    /**
     * 插入向量记录。
     *
     * @param chunkId 分块 ID
     * @param ownerId 所属用户 ID
     * @param embeddingLiteral 向量字面量（如 [0.1,0.2,...]）
     * @param model 模型标识
     * @param createdAt 创建时间
     * @return 插入行数
     */
    @Insert(
            "insert into document_chunk_embeddings (chunk_id, owner_id, embedding, model, created_at) "
                    + "values (#{chunkId}, #{ownerId}, cast(#{embeddingLiteral} as vector), #{model}, #{createdAt})")
    int insertEmbedding(
            @Param("chunkId") Long chunkId,
            @Param("ownerId") Long ownerId,
            @Param("embeddingLiteral") String embeddingLiteral,
            @Param("model") String model,
            @Param("createdAt") Instant createdAt);

    /**
     * Top-K 相似度检索（owner 过滤，仅返回已索引文档的分块）。
     *
     * @param ownerId 当前用户 ID
     * @param model 模型标识
     * @param queryEmbeddingLiteral 查询向量字面量
     * @param topK 返回条数
     * @return 命中列表
     */
    @Select(
            "select dc.id as chunkId, dc.document_id as documentId, dc.chunk_index as chunkIndex, dc.content as content "
                    + "from document_chunks dc "
                    + "join document_chunk_embeddings dce on dce.chunk_id = dc.id "
                    + "join documents d on d.id = dc.document_id "
                    + "where dc.owner_id = #{ownerId} and d.status = 'INDEXED' and dce.model = #{model} "
                    + "order by dce.embedding <-> cast(#{queryEmbeddingLiteral} as vector) "
                    + "limit #{topK}")
    List<ChunkSearchHit> searchTopK(
            @Param("ownerId") Long ownerId,
            @Param("model") String model,
            @Param("queryEmbeddingLiteral") String queryEmbeddingLiteral,
            @Param("topK") Integer topK);
}
