package com.zz.auth.aspect;

import com.zz.auth.recorder.OperationLogRecorder;
import com.zz.common.core.annotation.TraceRequest;
import com.zz.common.core.annotation.TraceStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * <p><b>认证服务-操作追踪切面（把业务方法编织成可记录的调用树）</b></p>
 *
 * <p>auth 是"埋点方"：通过 @Around 同时处理两类注解——</p>
 * <ul>
 *     <li>{@code @TraceRequest}（根）：在入口方法（如 LoginService.login）外围包一层，
 *         建主表记录 + 根步骤，结束时回写主表（见 {@link OperationLogRecorder#beginRequest} / {@link #endRequest}）；</li>
 *     <li>{@code @TraceStep}（子步骤）：包在链路上的方法外围（如 Feign 调用），
 *         建/结一条步骤记录（见 {@link OperationLogRecorder#beginStep} / {@link #endStep}）。
 *         步骤进行中发起的 Feign 请求会带上 X-Trace-* 头，让下游 system 把自己的步骤续挂在本步骤下。</li>
 * </ul>
 *
 * <h3>关键写法</h3>
 * <ol>
 *     <li>方法名取自 {@code ProceedingJoinPoint.getSignature().getName()}，
 *         module/callType/db/table 取自注解属性；</li>
 *     <li>try/catch/finally 语义：异常原样上抛（记录不吞业务异常）；
 *         收尾（endRequest/endStep 的弹栈与清空）在 catch 与正常路径都要走到，
 *         防止线程池复用后把上一次请求的 log_id 带到下一次请求。</li>
 * </ol>
 *
 * <h3>@TraceStep 标注 Feign 接口方法时的提醒</h3>
 * <p>OpenFeign 代理上的 AOP 在部分版本/代理顺序下可能不生效；若标注后无效果，
 * 请改用「门面包装」：在 auth 内新增 Spring 组件（如 SysUserFeignFacade），
 * 方法内只调一次对应 Feign 方法并把 @TraceStep 标在门面方法上
 * （原因与示例见 TraceStep 注解注释）。即使 feign 步骤没记上，登录根链路与
 * system 侧的续链仍能工作（头透传取自上下文栈顶，自动降级为挂根下）。</p>
 */
@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class OperationTraceAspect {

    /** 操作记录器：转发给 system-server 落库 */
    private final OperationLogRecorder recorder;

    /**
     * 请求级（根）环绕通知：处理 @TraceRequest 标注的方法
     *
     * @param pjp          连接点
     * @param traceRequest 方法上的 @TraceRequest 注解
     * @return 原方法返回值
     * @throws Throwable 原方法异常原样上抛
     */
    @Around("@annotation(traceRequest)")
    public Object aroundTraceRequest(ProceedingJoinPoint pjp, TraceRequest traceRequest) throws Throwable {
        String method = pjp.getSignature().getName();
        recorder.beginRequest(traceRequest.module(), method, traceRequest.callType());
        try {
            Object result = pjp.proceed();
            recorder.endRequest(OperationLogRecorder.STATUS_SUCCESS, null, null);
            return result;
        } catch (Throwable t) {
            recorder.endRequest(OperationLogRecorder.STATUS_FAIL, t.getMessage(), null);
            throw t;
        }
    }

    /**
     * 方法级（子步骤）环绕通知：处理 @TraceStep 标注的方法
     * <p>无外层请求（记录器返回 null）时直接放行，不产生孤儿步骤。</p>
     *
     * @param pjp       连接点
     * @param traceStep 方法上的 @TraceStep 注解
     * @return 原方法返回值
     * @throws Throwable 原方法异常原样上抛
     */
    @Around("@annotation(traceStep)")
    public Object aroundTraceStep(ProceedingJoinPoint pjp, TraceStep traceStep) throws Throwable {
        String method = pjp.getSignature().getName();
        Long stepId = recorder.beginStep(traceStep.module(), method, traceStep.callType(),
                traceStep.db(), traceStep.table());
        if (stepId == null) {
            return pjp.proceed();
        }
        try {
            Object result = pjp.proceed();
            recorder.endStep(stepId, OperationLogRecorder.STATUS_SUCCESS, null);
            return result;
        } catch (Throwable t) {
            recorder.endStep(stepId, OperationLogRecorder.STATUS_FAIL, t.getMessage());
            throw t;
        }
    }
}
