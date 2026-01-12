package com.jelvin.neopivot.auth.config;

import com.jelvin.neopivot.auth.application.JwtKeyService;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置。
 *
 * <p>约束：
 * <ul>
 *   <li>登录接口与 JWKS 端点允许匿名访问</li>
 *   <li>其余 API 默认需要用户级 JWT（方案A）</li>
 * </ul>
 *
 * @author Jelvin
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

    /**
     * 安全过滤链。
     *
     * @param http Spring Security HttpSecurity
     * @return 过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(
                auth ->
                        auth.requestMatchers("/api/auth/login", "/.well-known/jwks.json", "/actuator/health", "/actuator/info")
                                .permitAll()
                                .anyRequest()
                                .authenticated());

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * JWT 编码器（签发用，RS256）。
     *
     * @param jwtKeyService 密钥服务
     * @return 编码器
     */
    @Bean
    public JwtEncoder jwtEncoder(JwtKeyService jwtKeyService) {
        ImmutableJWKSet<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwtKeyService.privateJwks());
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JWT 解码器（验签用，RS256）。
     *
     * @param jwtKeyService 密钥服务
     * @return 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(JwtKeyService jwtKeyService) {
        return NimbusJwtDecoder.withPublicKey(jwtKeyService.publicKey()).build();
    }

    /**
     * 密码编码器（bcrypt）。
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
