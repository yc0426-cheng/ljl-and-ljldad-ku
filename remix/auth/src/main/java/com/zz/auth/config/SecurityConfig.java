package com.zz.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p><b>认证服务-Spring Security安全配置类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/28 14:52
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST 接口不走表单 CSRF token，必须关，否则 POST 一律被拦
                .csrf(AbstractHttpConfigurer::disable)
                // 关默认表单登录页（auth 有自己的 /auth/login 接口）
                .formLogin(AbstractHttpConfigurer::disable)
                // 关 HTTP Basic（项目用 Bearer Token 鉴权）
                .httpBasic(AbstractHttpConfigurer::disable)
                // 无状态会话（不创建 HttpSession，符合 JWT + Redis token 模型）
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 登录/登出/健康检查放行
                        .requestMatchers("/auth/login", "/auth/logout", "/actuator/**").permitAll()
                        // 暂时全放行，先把链路跑通；后续可改成 .anyRequest().authenticated() 走 JWT 校验
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}