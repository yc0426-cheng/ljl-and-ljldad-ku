package com.zz.common.core.trace;

/**
 * <p><b>通用核心-操作追踪请求头常量</b></p>
 *
 * <p>跨服务续链使用：调用方（auth 等）发起 Feign 请求前，从本线程 TraceContext 取出
 * log_id 与当前父步骤，写入下列请求头；被调模块（如 system）收到请求后从请求头恢复上下文，
 * 把自己执行的步骤挂到上游步骤之下，从而形成一棵跨服务的调用树。</p>
 *
 * <p>请求头由 Feign RequestInterceptor 自动写入（见 auth 模块 FeignClientTraceConfig），
 * 由被调模块的 TraceHeaderSeedFilter 读取（见 system 模块），业务代码无需感知。</p>
 *
 * <h3>判定规则</h3>
 * <ul>
 *     <li>请求头<b>带</b> X-Trace-Log-Id = 上游 feign 调过来的续链请求：只续写步骤、不建主表；</li>
 *     <li>请求头<b>不带</b> = 网关直达的新请求：入口模块负责建主表 + 根步骤。</li>
 * </ul>
 */
public final class TraceHeaders {

    /** 主表 log_id 请求头 */
    public static final String LOG_ID = "X-Trace-Log-Id";

    /** 上游父步骤 step_id 请求头（续链时挂载点） */
    public static final String PARENT_STEP_ID = "X-Trace-Parent-Step-Id";

    private TraceHeaders() {
    }
}
