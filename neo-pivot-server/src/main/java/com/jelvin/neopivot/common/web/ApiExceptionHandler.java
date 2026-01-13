package com.jelvin.neopivot.common.web;

import com.jelvin.neopivot.common.api.ApiErrorCode;
import com.jelvin.neopivot.common.api.ApiErrorDetail;
import com.jelvin.neopivot.common.api.ApiResponse;
import com.jelvin.neopivot.common.trace.ApiTraceId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>将业务异常统一转换为 {@link ApiResponse}，并保留合适的 HTTP 状态码语义。
 *
 * @author Jelvin
 */
@RestControllerAdvice(basePackages = "com.jelvin.neopivot")
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorDetail> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            ApiErrorDetail detail = new ApiErrorDetail();
            detail.setField(error.getField());
            detail.setMessage(error.getDefaultMessage());
            details.add(detail);
        }

        ApiResponse<Void> body = ApiResponse.error(ApiErrorCode.VALIDATION_ERROR, "参数校验失败", ApiTraceId.resolve(request));
        body.setDetails(details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ApiResponse<Void> body =
                ApiResponse.error(ApiErrorCode.VALIDATION_ERROR, "参数校验失败: " + ex.getMessage(), ApiTraceId.resolve(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex, HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ApiErrorCode.BAD_REQUEST, "请求参数错误", ApiTraceId.resolve(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ApiErrorCode.BAD_REQUEST, ex.getMessage(), ApiTraceId.resolve(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex, HttpServletRequest request) {
        HttpStatus status = resolveHttpStatus(ex);
        ApiErrorCode code = mapCode(status);
        String message = resolveMessage(ex, status);

        ApiResponse<Void> body = ApiResponse.error(code, message, ApiTraceId.resolve(request));
        return ResponseEntity.status(status).body(body);
    }

    private static HttpStatus resolveHttpStatus(Exception ex) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static ApiErrorCode mapCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ApiErrorCode.BAD_REQUEST;
            case UNAUTHORIZED -> ApiErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ApiErrorCode.FORBIDDEN;
            case NOT_FOUND -> ApiErrorCode.NOT_FOUND;
            case CONFLICT -> ApiErrorCode.CONFLICT;
            case GONE -> ApiErrorCode.GONE;
            case NOT_IMPLEMENTED -> ApiErrorCode.NOT_IMPLEMENTED;
            default -> ApiErrorCode.INTERNAL_ERROR;
        };
    }

    private static String resolveMessage(Exception ex, HttpStatus status) {
        String message = ex.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            String reason = responseStatus.reason();
            if (reason != null && !reason.isBlank()) {
                return reason;
            }
        }

        return switch (status) {
            case UNAUTHORIZED -> "未认证";
            case FORBIDDEN -> "无权限";
            case NOT_FOUND -> "资源不存在";
            case CONFLICT -> "资源冲突";
            case GONE -> "资源已过期";
            case NOT_IMPLEMENTED -> "接口尚未实现";
            case BAD_REQUEST -> "请求参数错误";
            default -> "服务器内部错误";
        };
    }
}

