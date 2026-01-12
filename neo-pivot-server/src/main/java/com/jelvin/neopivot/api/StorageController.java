package com.jelvin.neopivot.api;

import com.jelvin.neopivot.api.dto.PresignRequest;
import com.jelvin.neopivot.api.dto.PresignResponse;
import jakarta.validation.Valid;
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
public class StorageController {

    /**
     * 获取 presigned 上传凭证。
     *
     * @param request 预签名请求
     * @return 预签名响应
     */
    @PostMapping("/presign")
    public PresignResponse presign(@Valid @RequestBody PresignRequest request) {
        throw new ApiNotImplementedException("presign 尚未实现：后续将生成 s3://bucket/key 与 PUT presigned URL。");
    }
}

