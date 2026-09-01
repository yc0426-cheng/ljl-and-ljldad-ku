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
@Order(-999)  // 最先执行
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

        // 若是白名单的请求，直接放行
        if (authWhiteListProperties.getWhiteList().contains(method + " " + path)) {
            log.info("请求路径在白名单中，跳过认证：{}", method + " " + path);
            return chain.filter(exchange);
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
        ServerWebExchange mutated = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(userInfo.getUserId()))
                        .header("X-Account", userInfo.getAccount())
                        .header("X-User-Name", userInfo.getName())
                        .build())
                .build();
        return chain.filter(mutated);
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
