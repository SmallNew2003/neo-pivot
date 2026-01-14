package com.jelvin.neopivot.auth.infrastructure.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * JWT 密钥服务。
 *
 * <p>MVP 阶段使用进程内生成的 RSA KeyPair，并通过 JWKS 发布公钥。
 * 该方式便于快速跑通联调，但重启后旧 token 将失效。
 *
 * <p>生产环境建议：
 * <ul>
 *   <li>从外部密钥管理系统加载固定 keypair</li>
 *   <li>或至少从 keystore/PEM 注入</li>
 *   <li>并通过 kid + JWKS 支持轮换</li>
 * </ul>
 *
 * @author Jelvin
 */
@Component
public class JwtKeyService {

    private final RSAKey rsaKey;

    /**
     * 构造函数，生成一对 RSA 密钥。
     */
    public JwtKeyService() {
        this.rsaKey = generateRsaKey();
    }

    /**
     * 获取当前 RSA 私钥。
     *
     * @return 私钥
     */
    public RSAPrivateKey privateKey() {
        try {
            return rsaKey.toRSAPrivateKey();
        } catch (JOSEException exception) {
            throw new IllegalStateException("无法从 JWK 导出 RSA 私钥", exception);
        }
    }

    /**
     * 获取当前 RSA 公钥。
     *
     * @return 公钥
     */
    public RSAPublicKey publicKey() {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (JOSEException exception) {
            throw new IllegalStateException("无法从 JWK 导出 RSA 公钥", exception);
        }
    }

    /**
     * 获取 kid（密钥 ID）。
     *
     * @return kid
     */
    public String keyId() {
        return rsaKey.getKeyID();
    }

    /**
     * 获取 JWKS（仅包含公钥信息）。
     *
     * @return JWKS
     */
    public JWKSet publicJwks() {
        return new JWKSet(rsaKey.toPublicJWK());
    }

    /**
     * 获取内部使用的 JWKS（包含私钥信息）。
     *
     * <p>仅用于服务内部签发 JWT，不得对外暴露。
     *
     * @return JWKS（含私钥）
     */
    public JWKSet privateJwks() {
        return new JWKSet(rsaKey);
    }

    private static RSAKey generateRsaKey() {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK 不支持 RSA 算法", exception);
        }
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        String kid = UUID.randomUUID().toString();
        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(kid).build();
    }
}
