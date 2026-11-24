package com.yc.common.core.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "security.jwt")
class JwtProperties {
    var secret: String = ""
    var sessionExpireTime: Int = 0
}