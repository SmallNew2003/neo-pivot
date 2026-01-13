package com.jelvin.neopivot.storage.application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

/**
 * 对象存储 key 生成与文件名安全化服务。
 *
 * <p>约束见 OpenSpec 0016：
 * <ul>
 *   <li>storageUri：s3://neo-pivot/&lt;userId&gt;/&lt;documentId&gt;/&lt;filename&gt;</li>
 *   <li>filename：仅保留 basename，并过滤危险字符；用于 key 的文件名做 URL encode</li>
 * </ul>
 *
 * @author Jelvin
 */
@Service
public class StorageObjectKeyService {

    /**
     * 构造对象 key（不含 bucket）。
     *
     * @param userId 用户 ID（JWT sub）
     * @param documentId 文档 ID（预分配）
     * @param originalFilename 原始文件名
     * @return 对象 key（如 1/100/hello.pdf）
     */
    public String buildObjectKey(long userId, long documentId, String originalFilename) {
        String safeFilename = toSafeEncodedFilename(originalFilename);
        return userId + "/" + documentId + "/" + safeFilename;
    }

    /**
     * 将原始文件名转换为用于对象 key 的安全文件名。
     *
     * @param originalFilename 原始文件名
     * @return 安全且 URL encode 的文件名
     */
    public String toSafeEncodedFilename(String originalFilename) {
        String baseName = toBaseName(originalFilename);
        String sanitized = sanitize(baseName);
        String encoded = URLEncoder.encode(sanitized, StandardCharsets.UTF_8);
        return encoded.replace("+", "%20");
    }

    private static String toBaseName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        String normalized = filename.replace("\\", "/");
        int idx = normalized.lastIndexOf('/');
        String baseName = (idx >= 0) ? normalized.substring(idx + 1) : normalized;
        return baseName.isBlank() ? "file" : baseName;
    }

    private static String sanitize(String baseName) {
        String withoutTraversal = baseName.replace("..", ".");
        String filtered =
                withoutTraversal.chars()
                        .filter(ch -> ch >= 0x20 && ch != 0x7F)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();

        filtered = filtered.replace("/", "_").replace("\\", "_");
        if (filtered.isBlank()) {
            return "file";
        }
        if (filtered.length() > 200) {
            return filtered.substring(0, 200);
        }
        return filtered;
    }
}

