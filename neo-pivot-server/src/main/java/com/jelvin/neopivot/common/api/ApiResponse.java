package com.jelvin.neopivot.common.api;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一 API 响应体（Envelope）。
 *
 * <p>约定：
 * <ul>
 *   <li>成功：{@code success=true, code="0", message="OK"}</li>
 *   <li>失败：{@code success=false}，并携带 {@code code/message} 与可选 {@code details}</li>
 * </ul>
 *
 * @param <T> 数据类型
 * @author Jelvin
 */
@Getter
@Setter
public class ApiResponse<T> {

    private boolean success;

    private String code;

    private String message;

    private T data;

    private String traceId;

    private List<ApiErrorDetail> details;

    public static <T> ApiResponse<T> ok(T data, String traceId) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setCode(ApiErrorCode.OK.getCode());
        response.setMessage("OK");
        response.setData(data);
        response.setTraceId(traceId);
        return response;
    }

    public static <T> ApiResponse<T> error(ApiErrorCode code, String message, String traceId) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code.getCode());
        response.setMessage(message);
        response.setData(null);
        response.setTraceId(traceId);
        return response;
    }
}
