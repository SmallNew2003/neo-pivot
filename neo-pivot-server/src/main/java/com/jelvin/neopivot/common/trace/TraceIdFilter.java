package com.jelvin.neopivot.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * TraceId 过滤器。
 *
 * <p>将 traceId 写入：
 * <ul>
 *   <li>request attribute：{@link ApiTraceId#ATTRIBUTE_NAME}</li>
 *   <li>response header：{@link ApiTraceId#HEADER_NAME}</li>
 * </ul>
 *
 * @author Jelvin
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = ApiTraceId.resolve(request);
        request.setAttribute(ApiTraceId.ATTRIBUTE_NAME, traceId);
        response.setHeader(ApiTraceId.HEADER_NAME, traceId);
        filterChain.doFilter(request, response);
    }
}

