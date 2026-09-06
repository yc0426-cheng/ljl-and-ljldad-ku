package com.zz.common.core.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <p><b>通用工具-下游模块请求日志过滤器</b></p>
 * 网关鉴权通过后会把请求转发到下游模块（auth/system 等），本过滤器在各模块自身终端
 * 打印收到请求的日志，便于按模块观察请求落地情况。仅在 Servlet 类型应用激活，
 * 网关为 WebFlux 响应式应用，条件不满足不会被装配，因此不会在网关终端打印
 *
 * @author yangcheng
 * @since 2026/9/1 15:01
 */
@Slf4j
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RequestLogFilter implements Filter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ACCOUNT = "X-Account";
    private static final String HEADER_USER_NAME = "X-User-Name";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String method = httpRequest.getMethod();
            String path = httpRequest.getRequestURI();
            String userId = httpRequest.getHeader(HEADER_USER_ID);
            String account = httpRequest.getHeader(HEADER_ACCOUNT);
            // 姓名由网关注入时做过 URL 编码（HTTP 头不支持中文，直接放中文会变成 '?'），这里还原后再打印
            String userName = decodeHeader(httpRequest.getHeader(HEADER_USER_NAME));
            if (userId != null || account != null) {
                log.info("模块收到请求 | 路径:{} {} | 用户:id={},account={},name={}",
                        method, path, userId, account, userName);
            } else {
                log.info("模块收到请求 | 路径:{} {} | 用户:匿名", method, path);
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * 请求头值解码：与网关 encodeHeader 对应，用 UTF-8 URLDecoder 还原；
     * 解码失败（非 URL 编码的旧数据）时原样返回，避免打印异常
     *
     * @param value 可能已 URL 编码的头值
     * @return 解码后的原值；null 返回 null
     */
    private String decodeHeader(String value) {
        if (value == null) {
            return null;
        }
        try {
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.warn("用户姓名请求头解码失败，原样使用: {}", value);
            return value;
        }
    }

    @Override
    public int getOrder() {
        // 早于 Spring Security 默认顺序(-100)执行，确保所有进入模块的请求都被记录
        return -200;
    }
}
