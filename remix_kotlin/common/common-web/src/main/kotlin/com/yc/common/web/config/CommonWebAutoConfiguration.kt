package com.yc.common.web.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

/**
 * <p><b>通用核心工具-自动配置</b></p>
 * 自动注册统一响应包装、全局异常处理与 Feign 请求来源拦截器
 *
 * @author yangcheng
 * @since 2026/11/21
 */
@AutoConfiguration
@Import(GlobalResponseAdvice::class, GlobalExceptionHandler::class)
class CommonWebAutoConfiguration {

    /**
     * 注册 Feign 请求来源拦截器
     * 为所有 Feign 请求添加来源标记头，供下游识别服务间调用
     */
    @Bean
    @ConditionalOnClass(name = ["feign.RequestInterceptor"])
    @ConditionalOnMissingBean
    fun feignSourceInterceptor(): FeignSourceInterceptor = FeignSourceInterceptor()
}
