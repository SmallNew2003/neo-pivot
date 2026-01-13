package com.jelvin.neopivot.storage.application;

import com.jelvin.neopivot.storage.api.dto.PresignRequest;
import com.jelvin.neopivot.storage.api.dto.PresignResponse;
import com.jelvin.neopivot.storage.config.StorageS3Properties;
import com.jelvin.neopivot.storage.persistence.entity.StoragePresignEntity;
import com.jelvin.neopivot.storage.persistence.mapper.StoragePresignMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * 对象存储 presign 服务。
 *
 * <p>主路径：签发 presigned PUT，用于浏览器直传对象存储。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class StoragePresignService {

    private static final Logger log = LoggerFactory.getLogger(StoragePresignService.class);

    private final StorageS3Properties storageS3Properties;
    private final StorageObjectKeyService storageObjectKeyService;
    private final DocumentIdAllocatorService documentIdAllocatorService;
    private final S3Presigner s3Presigner;
    private final StoragePresignMapper storagePresignMapper;

    /**
     * 签发上传 presigned PUT。
     *
     * @param ownerId 用户 ID（JWT sub）
     * @param request 请求
     * @param issuedIp 签发时 IP
     * @param issuedUserAgent 签发时 UA
     * @return presign 响应
     */
    @Transactional
    public PresignResponse presignUpload(long ownerId, PresignRequest request, String issuedIp, String issuedUserAgent) {
        long documentId = documentIdAllocatorService.nextDocumentId();

        String bucket = storageS3Properties.getBucket();
        String key = storageObjectKeyService.buildObjectKey(ownerId, documentId, request.getFilename());
        String storageUri = "s3://" + bucket + "/" + key;

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder().bucket(bucket).key(key).contentType(request.getContentType()).build();
        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(storageS3Properties.getUploadPresignTtl())
                        .putObjectRequest(putObjectRequest)
                        .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(storageS3Properties.getUploadPresignTtl());

        StoragePresignEntity entity = new StoragePresignEntity();
        entity.setOwnerId(ownerId);
        entity.setDocumentId(documentId);
        entity.setPurpose("UPLOAD");
        entity.setMethod("PUT");
        entity.setStorageUri(storageUri);
        entity.setStatus("ISSUED");
        entity.setExpiresAt(expiresAt);
        entity.setIssuedIp(issuedIp);
        entity.setIssuedUserAgent(issuedUserAgent);
        entity.setCreatedAt(now);

        storagePresignMapper.insertSelective(entity);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", request.getContentType());

        PresignResponse response = new PresignResponse();
        response.setPresignId(String.valueOf(entity.getId()));
        response.setDocumentId(String.valueOf(documentId));
        response.setStorageUri(storageUri);
        response.setUploadMethod("PUT");
        response.setUploadUrl(presignedPutObjectRequest.url().toString());
        response.setHeaders(headers);
        response.setExpiresAt(expiresAt);

        log.info(
                "S3 presign issued: ownerId={}, presignId={}, documentId={}, storageUri={}, expiresAt={}",
                ownerId,
                entity.getId(),
                documentId,
                storageUri,
                expiresAt);

        return response;
    }
}
