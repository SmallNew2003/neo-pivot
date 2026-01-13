package com.jelvin.neopivot.document.application;

import com.jelvin.neopivot.common.events.DocumentUploadedEvent;
import com.jelvin.neopivot.document.api.dto.CreateDocumentRequest;
import com.jelvin.neopivot.document.api.dto.DocumentDto;
import com.jelvin.neopivot.document.persistence.entity.DocumentEntity;
import com.jelvin.neopivot.document.persistence.mapper.DocumentMapper;
import com.jelvin.neopivot.storage.persistence.entity.StoragePresignEntity;
import com.jelvin.neopivot.storage.persistence.mapper.StoragePresignMapper;
import com.mybatisflex.core.query.QueryWrapper;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 文档应用服务。
 *
 * <p>负责处理“上传确认（落库）”主路径：
 * <ul>
 *   <li>校验 presignId 属于当前用户且未过期、未被消费</li>
 *   <li>校验 documentId 与 storageUri 与 presign 记录一致</li>
 *   <li>写入 documents（初始状态 UPLOADED）并发布 DocumentUploadedEvent</li>
 * </ul>
 *
 * @author Jelvin
 */
@Service
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final StoragePresignMapper storagePresignMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 构造函数。
     *
     * @param documentMapper 文档 Mapper
     * @param storagePresignMapper 预签名记录 Mapper
     * @param applicationEventPublisher 事件发布器
     */
    public DocumentService(
            DocumentMapper documentMapper,
            StoragePresignMapper storagePresignMapper,
            ApplicationEventPublisher applicationEventPublisher) {
        this.documentMapper = documentMapper;
        this.storagePresignMapper = storagePresignMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 上传完成确认：创建文档记录并消费 presign。
     *
     * @param ownerId 当前用户 ID（JWT sub）
     * @param request 创建请求
     * @param consumedIp 消费时 IP
     * @return 文档 DTO
     */
    @Transactional
    public DocumentDto confirmUpload(long ownerId, CreateDocumentRequest request, String consumedIp) {
        long presignId = parseLongOrThrow(request.getPresignId(), "presignId");
        long documentId = parseLongOrThrow(request.getDocumentId(), "documentId");

        StoragePresignEntity presign = findPresignForOwner(presignId, ownerId);
        if (presign == null) {
            throw new PresignNotFoundException();
        }

        Instant now = Instant.now();
        if (presign.getExpiresAt() != null && presign.getExpiresAt().isBefore(now)) {
            if (!"EXPIRED".equals(presign.getStatus())) {
                presign.setStatus("EXPIRED");
                storagePresignMapper.update(presign);
            }
            throw new PresignExpiredException();
        }

        if (!"ISSUED".equals(presign.getStatus())) {
            if ("CONSUMED".equals(presign.getStatus())) {
                throw new PresignAlreadyConsumedException();
            }
            throw new PresignInvalidStateException();
        }

        if (presign.getDocumentId() == null || presign.getDocumentId() != documentId) {
            throw new PresignMismatchException();
        }
        if (presign.getStorageUri() == null || !presign.getStorageUri().equals(request.getStorageUri())) {
            throw new PresignMismatchException();
        }

        DocumentEntity entity = new DocumentEntity();
        entity.setId(documentId);
        entity.setOwnerId(ownerId);
        entity.setFilename(request.getFilename());
        entity.setContentType(request.getContentType());
        entity.setSizeBytes(request.getSizeBytes());
        entity.setSha256(request.getSha256());
        entity.setStorageUri(request.getStorageUri());
        entity.setStatus("UPLOADED");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        documentMapper.insertWithPk(entity);

        presign.setStatus("CONSUMED");
        presign.setConsumedAt(now);
        presign.setConsumedIp(consumedIp);
        storagePresignMapper.update(presign);

        applicationEventPublisher.publishEvent(new DocumentUploadedEvent(documentId));

        DocumentDto dto = new DocumentDto();
        dto.setId(String.valueOf(documentId));
        dto.setFilename(entity.getFilename());
        dto.setStorageUri(entity.getStorageUri());
        dto.setStatus(entity.getStatus());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private StoragePresignEntity findPresignForOwner(long presignId, long ownerId) {
        QueryWrapper query = QueryWrapper.create().where("id = ?", presignId).and("owner_id = ?", ownerId);
        return storagePresignMapper.selectOneByQuery(query);
    }

    private static long parseLongOrThrow(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " 必须为数字字符串");
        }
    }

    /**
     * presign 不存在或不属于当前用户：统一返回 404。
     *
     * @author Jelvin
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class PresignNotFoundException extends RuntimeException {}

    /**
     * presign 已过期：返回 410。
     *
     * @author Jelvin
     */
    @ResponseStatus(HttpStatus.GONE)
    private static class PresignExpiredException extends RuntimeException {}

    /**
     * presign 已被消费：返回 409。
     *
     * @author Jelvin
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    private static class PresignAlreadyConsumedException extends RuntimeException {}

    /**
     * presign 状态不合法：返回 400。
     *
     * @author Jelvin
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static class PresignInvalidStateException extends RuntimeException {}

    /**
     * presign 与请求字段不一致：返回 400。
     *
     * @author Jelvin
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static class PresignMismatchException extends RuntimeException {}
}
