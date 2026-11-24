package com.yc.common.web.config

import com.yc.common.core.constant.HttpHeaderConstant
import feign.RequestInterceptor
import feign.RequestTemplate

/**
 * <p><b>通用核心工具-Feign 请求来源拦截器</b></p>
 * 为所有 Feign 请求添加 [HttpHeaderConstant.REQUEST_SOURCE] 标记头，
 * 下游服务据此识别服务间调用，跳过统一响应包装
 *
 * @author yangcheng
 * @since 2026/11/21
 */
class FeignSourceInterceptor : RequestInterceptor {

    /**
     * 为请求模板添加 Feign 来源标记头
     *
     * @param template 请求模板
     */
    override fun apply(template: RequestTemplate) {
        template.header(HttpHeaderConstant.REQUEST_SOURCE, HttpHeaderConstant.SOURCE_FEIGN)
    }
}
