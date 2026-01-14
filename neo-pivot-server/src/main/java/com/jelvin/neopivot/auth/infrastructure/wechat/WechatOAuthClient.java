package com.jelvin.neopivot.auth.infrastructure.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jelvin.neopivot.auth.config.AuthProperties;
import com.jelvin.neopivot.auth.exception.AuthLoginFailedException;
import com.jelvin.neopivot.auth.exception.WechatNotConfiguredException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 微信 OAuth2 授权码换取外部主体信息的客户端。
 *
 * @author Jelvin
 */
@Service
@RequiredArgsConstructor
public class WechatOAuthClient {

    private final AuthProperties authProperties;

    /**
     * 使用授权码换取外部主体标识（优先 unionid，其次 openid）。
     *
     * @param code 微信授权码
     * @return externalSubject（unionid/openid）
     */
    public String exchangeCodeForSubject(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code 不能为空");
        }

        AuthProperties.WechatProperties wechat = authProperties.getWechat();
        if (wechat == null
                || wechat.getAppId() == null
                || wechat.getAppId().isBlank()
                || wechat.getAppSecret() == null
                || wechat.getAppSecret().isBlank()) {
            throw new WechatNotConfiguredException();
        }

        RestClient client = RestClient.builder().baseUrl(wechat.getBaseUrl()).build();
        WechatAccessTokenResponse response =
                client.get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/sns/oauth2/access_token")
                                                .queryParam("appid", wechat.getAppId())
                                                .queryParam("secret", wechat.getAppSecret())
                                                .queryParam("code", code.trim())
                                                .queryParam("grant_type", "authorization_code")
                                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(WechatAccessTokenResponse.class);

        if (response == null) {
            throw new AuthLoginFailedException();
        }
        if (response.errcode != null && response.errcode != 0) {
            throw new AuthLoginFailedException();
        }

        String subject = response.unionid == null || response.unionid.isBlank() ? response.openid : response.unionid;
        if (subject == null || subject.isBlank()) {
            throw new AuthLoginFailedException();
        }
        return subject;
    }

    /**
     * 微信 access_token 响应（只取最小必要字段）。
     *
     * @author Jelvin
     */
    public record WechatAccessTokenResponse(
            String openid,
            String unionid,
            Integer errcode,
            @JsonProperty("errmsg") String errMsg) {}
}
