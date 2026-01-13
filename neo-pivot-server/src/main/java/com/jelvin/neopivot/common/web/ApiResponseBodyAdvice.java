package com.jelvin.neopivot.common.web;

import com.jelvin.neopivot.common.api.ApiResponse;
import com.jelvin.neopivot.common.trace.ApiTraceId;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一成功响应体包装器。
 *
 * <p>将业务 Controller 的正常返回值自动包装为 {@link ApiResponse}。
 *
 * @author Jelvin
 */
@Order(0)
@RestControllerAdvice(basePackages = "com.jelvin.neopivot")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof ApiResponse<?>) {
            return body;
        }

        if (body instanceof byte[] || body instanceof org.springframework.core.io.Resource) {
            return body;
        }

        if (body instanceof String) {
            return body;
        }

        String traceId = extractTraceId(request);
        return ApiResponse.ok(body, traceId);
    }

    private static String extractTraceId(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return ApiTraceId.resolve(servletRequest.getServletRequest());
        }
        return null;
    }
}

