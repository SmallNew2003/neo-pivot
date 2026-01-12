package com.jelvin.neopivot.document.api;

import com.jelvin.neopivot.common.api.ApiNotImplementedException;
import com.jelvin.neopivot.document.api.dto.CreateDocumentRequest;
import com.jelvin.neopivot.document.api.dto.DocumentDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档管理接口。
 *
 * <p>骨架阶段仅占位，后续将按 OpenSpec 完成：
 * <ul>
 *   <li>S3 presigned 直传 + 回调落库触发索引</li>
 *   <li>文档状态机（UPLOADED/INDEXING/INDEXED/FAILED）</li>
 * </ul>
 *
 * @author Jelvin
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    /**
     * 创建文档记录（方式二：提交 storageUri 元数据）。
     *
     * @param request 创建文档请求
     * @return 文档信息
     */
    @PostMapping
    public DocumentDto create(@Valid @RequestBody CreateDocumentRequest request) {
        throw new ApiNotImplementedException("文档创建尚未实现：将支持 storageUri 落库并发布 DocumentUploadedEvent。");
    }

    /**
     * 查询当前用户文档列表。
     *
     * @return 文档列表
     */
    @GetMapping
    public List<DocumentDto> list() {
        throw new ApiNotImplementedException("文档列表尚未实现。");
    }

    /**
     * 查询文档详情。
     *
     * @param documentId 文档 ID
     * @return 文档详情
     */
    @GetMapping("/{documentId}")
    public DocumentDto get(@PathVariable String documentId) {
        throw new ApiNotImplementedException("文档详情尚未实现。");
    }
}
