package com.zz.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p><b>认证服务-安全配置</b></p>
 *
 * <p>关闭 Spring Security 默认拦截（表单登录、HTTP Basic、CSRF），
 * 全部请求放行。真正的令牌校验由 {@code common-redis} 里的
 * TokenAuthFilter 完成（登录态存于 Redis）。</p>
 *
 * @author yangcheng
 * @since 2026/8/22
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 无状态：不创建 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 关闭跨站请求伪造防护（接口由令牌校验）
                .csrf(csrf -> csrf.disable())
                // 关闭表单登录与 HTTP Basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 所有请求放行，鉴权交给 TokenAuthFilter
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
