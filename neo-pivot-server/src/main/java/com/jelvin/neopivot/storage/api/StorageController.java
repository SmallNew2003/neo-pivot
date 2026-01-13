package com.jelvin.neopivot.storage.api;

import com.jelvin.neopivot.storage.application.StoragePresignService;
import com.jelvin.neopivot.storage.api.dto.PresignRequest;
import com.jelvin.neopivot.storage.api.dto.PresignResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对象存储相关接口。
 *
 * <p>主路径：S3 presigned PUT 直传（见 OpenSpec 0010）。
 *
 * @author Jelvin
 */
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StoragePresignService storagePresignService;

    /**
     * 获取 presigned 上传凭证。
     *
     * @param request 预签名请求
     * @return 预签名响应
     */
    @PostMapping("/presign")
    public PresignResponse presign(
            @Valid @RequestBody PresignRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest) {
        long ownerId = Long.parseLong(jwt.getSubject());
        String issuedIp = httpServletRequest.getRemoteAddr();
        String issuedUserAgent = httpServletRequest.getHeader("User-Agent");
        return storagePresignService.presignUpload(ownerId, request, issuedIp, issuedUserAgent);
    }
}
