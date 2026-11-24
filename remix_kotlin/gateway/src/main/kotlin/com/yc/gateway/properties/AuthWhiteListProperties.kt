package com.yc.gateway.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * <p><b>网关服务-白名单配置类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/1 12:01
 */
@Component
@ConfigurationProperties(prefix = "auth")
class AuthWhiteListProperties {

    var whiteList: List<String> = emptyList()
}