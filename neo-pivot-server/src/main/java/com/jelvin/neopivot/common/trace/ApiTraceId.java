package com.jelvin.neopivot.common.trace;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * TraceId 透传与生成工具。
 *
 * <p>规则：
 * <ul>
 *   <li>优先读取请求头 {@code X-Request-Id}</li>
 *   <li>若不存在则生成 UUID（去掉连字符）</li>
 * </ul>
 *
 * @author Jelvin
 */
public final class ApiTraceId {

    public static final String HEADER_NAME = "X-Request-Id";

    public static final String ATTRIBUTE_NAME = ApiTraceId.class.getName() + ".traceId";

    private ApiTraceId() {}

    public static String resolve(HttpServletRequest request) {
        Object attribute = request.getAttribute(ATTRIBUTE_NAME);
        if (attribute instanceof String value && !value.isBlank()) {
            return value;
        }

        String fromHeader = request.getHeader(HEADER_NAME);
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader;
        }

        return UUID.randomUUID().toString().replace("-", "");
    }
}

