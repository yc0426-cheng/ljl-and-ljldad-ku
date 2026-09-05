package com.zz.system.trace;

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
 * <p><b>系统服务-操作追踪切面</b></p>
 *
 * <p>system 是"落库方 + 被调续链方"：本切面负责把本模块内的业务方法编织成可记录的步骤。
 * 上下文来源有两种：</p>
 * <ul>
 *     <li>TraceHeaderSeedFilter 从请求头恢复（auth 的 feign 续链请求）→ 步骤挂到上游父步骤下；</li>
 *     <li>本模块未来网关直达入口的 @TraceRequest → 建主表 + 根步骤（见 beginRequest/endRequest）。</li>
 * </ul>
 *
 * <p>当前实际接入：SysUserServiceImpl 的方法标注了 @TraceStep（登录时被 feign 触发），
 * 步骤挂在 auth 那侧 feign 步骤之下，形成跨服务调用树。</p>
 */
@Slf4j
@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class SystemOperationTraceAspect {

    /** 本地记录器（直接落库到 system） */
    private final SystemOperationLogRecorder recorder;

    /**
     * 请求级（根）环绕通知：处理 @TraceRequest 标注的方法
     * <p>当前 system 无 @TraceRequest 接入（无网关直达入口），逻辑为将来预留：
     * 入口模块语义，建主表 + 根步骤并在结束收尾（异常原样上抛）。</p>
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
            recorder.endRequest(SystemOperationLogRecorder.STATUS_SUCCESS, null, null);
            return result;
        } catch (Throwable t) {
            recorder.endRequest(SystemOperationLogRecorder.STATUS_FAIL, t.getMessage(), null);
            throw t;
        }
    }

    /**
     * 方法级（子步骤）环绕通知：处理 @TraceStep 标注的方法
     *
     * <p>无追踪上下文（网关直达且无 @TraceRequest、或非追踪链路调用）时直接放行，不产生孤儿步骤。</p>
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
            recorder.endStep(stepId, SystemOperationLogRecorder.STATUS_SUCCESS, null);
            return result;
        } catch (Throwable t) {
            recorder.endStep(stepId, SystemOperationLogRecorder.STATUS_FAIL, t.getMessage());
            throw t;
        }
    }
}
