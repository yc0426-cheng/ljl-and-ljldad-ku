package com.zz.system.trace;

import com.zz.common.core.trace.TraceContext;
import com.zz.common.core.trace.TraceHeaders;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * <p><b>系统服务-追踪请求头续链过滤器</b></p>
 *
 * <p>跨服务续链的接收端：上游（auth）发 Feign 请求时会带 X-Trace-Log-Id /
 * X-Trace-Parent-Step-Id 两个请求头（由 auth 的 Feign RequestInterceptor 自动写入）。
 * 本过滤器在请求进入本模块时读取请求头并初始化 TraceContext：</p>
 * <ul>
 *     <li>带 X-Trace-Log-Id → 这是上游续链请求：{@code TraceContext.begin(logId, 父步骤id)}，
 *         之后本模块内标注 @TraceStep 的方法会把步骤挂到上游那步下面（<b>不建主表</b>）；</li>
 *     <li>不带 → 网关直达的新请求：不初始化（留空），由入口 @TraceRequest 负责建主表；
 *         本模块当前没有用户直连入口，若以后新增网关直达的 system 接口，按注释在入口方法加 @TraceRequest 即可。</li>
 * </ul>
 *
 * <p>过滤结束后（含异常）必须清空上下文，防止线程池复用串号。</p>
 */
@Slf4j
@Component
public class TraceHeaderSeedFilter implements Filter, Ordered {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean seeded = false;
        if (request instanceof HttpServletRequest httpRequest) {
            String logIdStr = httpRequest.getHeader(TraceHeaders.LOG_ID);
            if (StringUtils.hasText(logIdStr)) {
                Long logId = parseLong(logIdStr);
                if (logId != null) {
                    Long parentStepId = parseLong(httpRequest.getHeader(TraceHeaders.PARENT_STEP_ID));
                    TraceContext.begin(logId, parentStepId);
                    seeded = true;
                    log.info("追踪续链 log_id={} parent_step_id={} 路径:{} {}", logId, parentStepId,
                            httpRequest.getMethod(), httpRequest.getRequestURI());
                }
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            if (seeded) {
                TraceContext.clear();
            }
        }
    }

    /**
     * 字符串安全转 Long
     *
     * @param text 字符串
     * @return Long 或 null
     */
    private Long parseLong(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            log.warn("追踪请求头解析失败: {}", text);
            return null;
        }
    }

    @Override
    public int getOrder() {
        // 早于 common-core 的 RequestLogFilter(-200)，确保业务切面执行前上下文已就绪
        return -300;
    }
}
