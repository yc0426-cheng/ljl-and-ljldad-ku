package com.zz.gateway.filter;

import com.zz.common.core.constant.RedisKeyConstant;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.common.redis.service.RedisService;
import com.zz.gateway.enums.FilterExceptionEnum;
import com.zz.gateway.properties.AuthWhiteListProperties;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * <p><b>网关服务-全局过滤器</b></p>
 *
 * @author yangcheng
 * @since 2026/8/30 13:24
 */
@Slf4j
@Order(-1)  // 执行顺序 不得小于 -2
@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter {

    private final RedisService redisService;

    private final AuthWhiteListProperties  authWhiteListProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求路径
        String path = exchange.getRequest().getURI().getPath();
        // 获取请求类型 'POST'
        String method = exchange.getRequest().getMethod().name();
        log.info("请求路径:{},{}, 请求IP地址:{}", method, path, getClientIP(exchange));

        // 若是白名单的请求：直接放行，不再强制鉴权；
        // 但若请求恰好携带了有效 token（如登录后的 /auth/check、/auth/logout），
        // 仍尽力把用户信息注入下游请求头，让下游模块日志能显示真实用户（而非"匿名"）
        if (authWhiteListProperties.getWhiteList().contains(method + " " + path)) {
            log.info("请求路径在白名单中，跳过认证：{}", method + " " + path);
            return chain.filter(maybeInjectUser(exchange));
        }

        // 获取token
        String token = extractToken(exchange);
        if (Objects.isNull(token)) {
            logGatewayError(exchange, FilterExceptionEnum.TOKEN_NOT_EXISTS);
            return Mono.error(new BizException(FilterExceptionEnum.TOKEN_NOT_EXISTS));
        }

        // 查 Redis 拿用户信息，没有就抛异常
        LoginUserInfo userInfo = redisService.get(
                RedisKeyConstant.TOKEN + token, LoginUserInfo.class);
        if (userInfo == null) {
            logGatewayError(exchange, FilterExceptionEnum.USER_NOT_EXISTS);
            return Mono.error(new BizException(FilterExceptionEnum.USER_NOT_EXISTS));
        }
        log.info("当前用户为:{}", userInfo);

        // 把用户信息透传给下游 —— mutate 请求头
        return chain.filter(injectUserHeaders(exchange, userInfo));
    }

    /**
     * 白名单请求的"可选注入"：请求携带 Authorization 且能在 redis 查到用户时，
     * 注入 X-User-* 头（让 /auth/check、/auth/logout 等白名单接口的日志能显示用户）；
     * 无 token / token 无效 / redis 查询异常时原样返回（白名单仍放行，不影响登录等匿名入口）。
     *
     * @param exchange 请求上下文
     * @return 注入后的 exchange；无需注入时原样返回
     */
    private ServerWebExchange maybeInjectUser(ServerWebExchange exchange) {
        String token = extractToken(exchange);
        if (Objects.isNull(token)) {
            return exchange;
        }
        try {
            LoginUserInfo userInfo = redisService.get(
                    RedisKeyConstant.TOKEN + token, LoginUserInfo.class);
            if (userInfo == null) {
                // 白名单且 token 无效（如首次登录尚无 token）：保持匿名放行
                return exchange;
            }
            log.info("白名单请求携带有效 token，注入用户:{}", userInfo);
            return injectUserHeaders(exchange, userInfo);
        } catch (Exception e) {
            // 可选注入失败不阻断白名单请求
            log.warn("白名单请求用户注入失败，保持匿名放行：{}", exchange.getRequest().getURI().getPath());
            return exchange;
        }
    }

    /**
     * 把用户信息写入下游请求头（X-User-Id / X-Account / X-User-Name），供下游模块读取
     * <p>姓名可能含中文，HTTP 头只允许 ISO-8859-1，直接写入会把中文替换成 '?'，
     * 因此姓名先 URL 编码（纯 ASCII）再放行，下游读取时 URLDecoder 还原。</p>
     *
     * @param exchange  请求上下文
     * @param userInfo  登录用户信息
     * @return 携带用户头的 exchange
     */
    private ServerWebExchange injectUserHeaders(ServerWebExchange exchange, LoginUserInfo userInfo) {
        return exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(userInfo.getUserId()))
                        .header("X-Account", userInfo.getAccount())
                        .header("X-User-Name", encodeHeader(userInfo.getName()))
                        .build())
                .build();
    }

    /**
     * 请求头值安全编码：null 原样返回，非 null 用 UTF-8 URL 编码为纯 ASCII
     *
     * @param value 原始值（可能含中文）
     * @return 编码后的 ASCII 值；null 返回 null
     */
    private String encodeHeader(String value) {
        if (value == null) {
            return null;
        }
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 打印网关拦截异常日志（仅在网关终端输出请求路径与错误信息）
     *
     * @param exchange  请求上下文
     * @param errorEnum 过滤器异常枚举
     */
    private void logGatewayError(ServerWebExchange exchange, FilterExceptionEnum errorEnum) {
        String path = exchange.getRequest().getURI().getPath();
        log.error("网关拦截异常 | 路径:{} | 错误码:{} | 错误信息:{}",
                path, errorEnum.getErrorCode(), errorEnum.getErrorMessage());
    }

    /**
     * 获取客户端IP
     *
     * @return ip
     */
    private String getClientIP(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.isEmpty(ip)) {
            ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
        }
        return ip;
    }

    /**
     * 获取token
     *
     * @return token
     */
    private String extractToken(ServerWebExchange exchange) {
        // 获取请求头,拿到 `authorization`
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        // 判断是否是 JWT 令牌
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        // 判断是否为 基础账号密码认证
        if (authorization != null && authorization.startsWith("Basic ")) {
            return authorization.substring(6);
        }

        return null;
    }
}
