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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * AuthenticationEntryPoint：未认证（401）统一响应。
 *
 * @author Jelvin
 */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 构造函数。
     *
     * @param objectMapper JSON 序列化器
     */
    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {
        String traceId = ApiTraceId.resolve(request);
        ApiResponse<Void> body = ApiResponse.error(ApiErrorCode.UNAUTHORIZED, "未认证", traceId);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}

