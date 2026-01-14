package com.jelvin.neopivot.auth.api;

import com.jelvin.neopivot.auth.api.dto.OtpSendRequest;
import com.jelvin.neopivot.auth.api.dto.OtpSendResponse;
import com.jelvin.neopivot.auth.api.dto.LoginRequest;
import com.jelvin.neopivot.auth.api.dto.LoginResponse;
import com.jelvin.neopivot.auth.application.login.AuthLoginService;
import com.jelvin.neopivot.auth.infrastructure.otp.OtpService;
import com.jelvin.neopivot.common.api.ApiResponse;
import com.jelvin.neopivot.common.trace.ApiTraceId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关 API。
 *
 * <p>提供标准登录接口，用于获取用户级 JWT，支撑：
 * <ul>
 *   <li>前端管理台标准登录</li>
 *   <li>平台接入方案A（透传终端用户 JWT）</li>
 * </ul>
 *
 * @author Jelvin
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthLoginService authLoginService;
    private final OtpService otpService;

    /**
     * 统一登录：按 grantType 认证并签发用户级 JWT。
     *
     * @param request 登录请求
     * @param httpServletRequest HTTP 请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {
        String traceId = ApiTraceId.resolve(httpServletRequest);
        httpServletRequest.setAttribute(ApiTraceId.ATTRIBUTE_NAME, traceId);

        String ip = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        LoginResponse response = authLoginService.login(request, traceId, ip, userAgent);
        return ApiResponse.ok(response, traceId);
    }

    /**
     * 发送验证码：创建 OTP challenge 并触发验证码发送（MVP 最小实现为创建 challenge + 返回 challengeId）。
     *
     * @param request OTP 发送请求
     * @param httpServletRequest HTTP 请求
     * @return OTP 发送响应
     */
    @PostMapping("/otp/send")
    public ApiResponse<OtpSendResponse> sendOtp(
            @Valid @RequestBody OtpSendRequest request,
            HttpServletRequest httpServletRequest) {
        String traceId = ApiTraceId.resolve(httpServletRequest);
        httpServletRequest.setAttribute(ApiTraceId.ATTRIBUTE_NAME, traceId);

        OtpService.OtpIssueResult issued = otpService.issue(request.getTenantCode(), request.getChannel(), request.getTarget());

        OtpSendResponse response = new OtpSendResponse();
        response.setChallengeId(issued.challengeId());
        response.setExpiresInSeconds(issued.expiresInSeconds());
        return ApiResponse.ok(response, traceId);
    }
}
