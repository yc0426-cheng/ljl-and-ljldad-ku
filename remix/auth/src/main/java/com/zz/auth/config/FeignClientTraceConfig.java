package com.zz.auth.config;

import com.zz.common.core.trace.TraceContext;
import com.zz.common.core.trace.TraceHeaders;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p><b>认证服务-Feign 追踪请求头配置</b></p>
 *
 * <p>当前线程处于操作追踪中（TraceContext 有 log_id）时，自动给外发的每个 Feign 请求
 * 附加 X-Trace-Log-Id / X-Trace-Parent-Step-Id 请求头，把本模块的追踪上下文透传给下游模块：
 * 下游（system）的 TraceHeaderSeedFilter 据此续链，把自己执行的步骤挂到本模块
 * "当前正在执行的步骤"之下，从而拼出跨服务的调用树。</p>
 *
 * <p>实现机制：Feign 会收集容器内所有 RequestInterceptor Bean 应用到每次请求，
 * 业务代码无需任何改动。</p>
 */
@Configuration
public class FeignClientTraceConfig {

    /**
     * 追踪请求头拦截器 Bean
     *
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor traceHeaderRequestInterceptor() {
        return template -> {
            if (TraceContext.hasTrace()) {
                Long logId = TraceContext.currentLogId();
                if (logId != null) {
                    template.header(TraceHeaders.LOG_ID, String.valueOf(logId));
                }
                Long parentStepId = TraceContext.currentParentStepId();
                if (parentStepId != null) {
                    template.header(TraceHeaders.PARENT_STEP_ID, String.valueOf(parentStepId));
                }
            }
        };
    }
}
