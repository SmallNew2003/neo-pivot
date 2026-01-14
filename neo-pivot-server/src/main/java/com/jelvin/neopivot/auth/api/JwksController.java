package com.jelvin.neopivot.auth.api;

import com.jelvin.neopivot.auth.infrastructure.jwt.JwtKeyService;
import com.nimbusds.jose.jwk.JWKSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWKS 发布端点。
 *
 * <p>用于资源服务通过 kid 轮换验签公钥，对应规范：
 * `openspec/specs/0003-auth-login-spec.md`。
 *
 * @author Jelvin
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwtKeyService jwtKeyService;

    /**
     * 发布 JWKS。
     *
     * @return JWKS JSON
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        JWKSet jwkSet = jwtKeyService.publicJwks();
        return jwkSet.toJSONObject();
    }
}
