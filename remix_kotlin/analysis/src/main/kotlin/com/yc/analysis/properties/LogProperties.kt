package com.yc.analysis.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "log.root")
class LogProperties {

    var path: String = ""
}