package com.jelvin.neopivot.auth.config;

import com.jelvin.neopivot.auth.application.JwtKeyService;
import com.jelvin.neopivot.common.security.ApiAccessDeniedHandler;
import com.jelvin.neopivot.common.security.ApiAuthenticationEntryPoint;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
import org.springframework.validation.annotation.Validated;

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
@EnableConfigurationProperties({AuthProperties.class, SecurityConfig.SecurityWhitelistProperties.class})
public class SecurityConfig {

    /**
     * 安全过滤链。
     *
     * @param http Spring Security HttpSecurity
     * @param whitelistProperties 白名单配置
     * @return 过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityWhitelistProperties whitelistProperties,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            ApiAccessDeniedHandler accessDeniedHandler)
            throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> {
            List<String> permitAll = whitelistProperties.getPermitAll();
            if (permitAll != null && !permitAll.isEmpty()) {
                auth.requestMatchers(permitAll.toArray(new String[0])).permitAll();
            }
            auth.anyRequest().authenticated();
        });

        http.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler));
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

    /**
     * Spring Security 白名单配置。
     *
     * 说明：
     * 1) permitAll 用于维护允许匿名访问的路径模式（Ant 风格）。
     * 2) 建议通过环境变量配置，例如：NEOPIVOT_SECURITY_PERMIT_ALL
     * 3) 本地默认值在 application.yml 中提供，环境变量会覆盖默认值。
     *
     * @author Jelvin
     */
    @Validated
    @ConfigurationProperties(prefix = "neopivot.security")
    @Getter
    @Setter
    public static class SecurityWhitelistProperties {

        private List<String> permitAll = new ArrayList<>();
    }
}
