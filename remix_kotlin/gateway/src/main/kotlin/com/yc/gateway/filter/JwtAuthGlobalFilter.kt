package com.yc.gateway.filter

import com.yc.common.core.constant.RedisKeyConstant
import com.yc.common.core.exception.BaseException
import com.yc.common.core.pojo.LoginUserInfo
import com.yc.commonRedis.service.RedisService
import com.yc.gateway.enums.FilterExceptionEnum
import com.yc.gateway.properties.AuthWhiteListProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * <p><b>网关服务-全局过滤器</b></p>
 *
 * @author yangcheng
 * @since 2026/11/18 21:24
 */
@Order(-1) // 执行顺序 不得小于 -2
@Component
class JwtAuthGlobalFilter(
    private val redisService: RedisService,
    private val authWhiteListProperties: AuthWhiteListProperties
) : GlobalFilter {

    companion object {
        val log: Logger? = LoggerFactory.getLogger(this::class.java)
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        val method = exchange.request.method.name()
        log?.info("请求路径:$method,$path, 请求IP地址:${getClientIP(exchange)}")

        if (authWhiteListProperties.whiteList.contains("$method $path")) {
            log?.info("请求路径在白名单中，跳过认证：$method $path")
            return chain.filter(exchange)
        }

        val token = extractToken(exchange)
        if (token == null) {
            logGatewayError(exchange, FilterExceptionEnum.TOKEN_NOT_EXISTS)
            return Mono.error { BaseException.BizException(FilterExceptionEnum.TOKEN_NOT_EXISTS) }
        }

        val userInfo = redisService.get(
            RedisKeyConstant.TOKEN + token, LoginUserInfo::class.java
        )

        if (userInfo == null) {
            logGatewayError(exchange, FilterExceptionEnum.USER_NOT_EXISTS)
            return Mono.error { BaseException.BizException(FilterExceptionEnum.USER_NOT_EXISTS) }
        }
        log?.info("当前用户为：$userInfo")

        return chain.filter(maybeInjectUser(exchange))
    }

    /**
     * 白名单请求的"可选注入"：请求携带 Authorization 且能在 redis 查到用户时，
     * 注入 X-User-* 头（让 /auth/check、/auth/logout 等白名单接口的日志能显示用户）；
     * 无 token / token 无效 / redis 查询异常时原样返回（白名单仍放行，不影响登录等匿名入口）。
     *
     * @param exchange 请求上下文
     * @return 注入后的 exchange；无需注入时原样返回
     */
    private fun maybeInjectUser(exchange: ServerWebExchange): ServerWebExchange {
        val token = extractToken(exchange)
        if (token.isNullOrBlank()) return exchange
        try {
            val userInfo = redisService.get(
                RedisKeyConstant.TOKEN + token, LoginUserInfo::class.java
            ) ?: return exchange
            log?.info("白名单请求携带有效 token, 注入用户: ${userInfo.account}")
            return injectUserHeaders(exchange, userInfo)
        } catch (_: Exception) {
            log?.warn("白名单请求用户注入失败, 保持匿名放行: ${exchange.request.uri.path}")
            return exchange
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
    private fun injectUserHeaders(exchange: ServerWebExchange, userInfo: LoginUserInfo): ServerWebExchange {
        return exchange.mutate()
            .request(
                exchange.request.mutate()
                    .header("X-User-Id", userInfo.userId.toString())
                    .header("X-Account", userInfo.account)
                    .header("X-User-Name", encodeHeader(userInfo.name))
                    .build()
            )
            .build()
    }

    /**
     * 请求头值安全编码：null 原样返回，非 null 用 UTF-8 URL 编码为纯 ASCII
     *
     * @param value 原始值（可能含中文）
     * @return 编码后的 ASCII 值；null 返回 null
     */
    private fun encodeHeader(value: String?): String? {
        value ?: return null
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
    }

    /**
     * 打印网关拦截异常日志（仅在网关终端输出请求路径与错误信息）
     *
     * @param exchange  请求上下文
     * @param errorEnum 过滤器异常枚举
     */
    private fun logGatewayError(exchange: ServerWebExchange, errorEnum: FilterExceptionEnum) {
        val path = exchange.request.uri.path
        log?.error(
            "网关拦截异常 | 路径:$path | 错误码:${errorEnum.code} | 错误信息:${errorEnum.errorMessage}"
        )
    }

    /**
     * 获取客户端IP
     *
     * @return ip
     */
    private fun getClientIP(exchange: ServerWebExchange): String {
        var ip = exchange.request.headers.getFirst("X-Forwarded-For")
        if (ip.isNullOrBlank()) {
            ip = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
        }
        return ip

    }

    /**
     * 获取token
     *
     * @return token
     */
    private fun extractToken(exchange: ServerWebExchange): String? {
        val headers = exchange.request.headers
        val authorization = headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7)
        }

        if (authorization != null && authorization.startsWith("Basic ")) {
            return authorization.substring(7)
        }

        return null
    }
}