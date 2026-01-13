package com.jelvin.neopivot.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelvin.neopivot.common.api.ApiErrorCode;
import com.jelvin.neopivot.common.api.ApiResponse;
import com.jelvin.neopivot.common.trace.ApiTraceId;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * AccessDeniedHandler：无权限（403）统一响应。
 *
 * @author Jelvin
 */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 构造函数。
     *
     * @param objectMapper JSON 序列化器
     */
    public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        String traceId = ApiTraceId.resolve(request);
        ApiResponse<Void> body = ApiResponse.error(ApiErrorCode.FORBIDDEN, "无权限", traceId);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}

