package com.yc.common.core.constant

/**
 * <p><b>通用核心工具-HTTP 请求头常量</b></p>
 *
 * @author yangcheng
 * @since 2026/11/21
 */
object HttpHeaderConstant {

    /** 请求来源标记头 */
    const val REQUEST_SOURCE = "X-Request-Source"

    /** 请求来源：Feign 服务间调用 */
    const val SOURCE_FEIGN = "feign"
}
