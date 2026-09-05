package com.zz.auth.recorder;

import com.zz.api.system.operation.OperationLogFeignClient;
import com.zz.common.core.trace.TraceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p><b>认证服务-操作日志记录器（调用方视角，经 Feign 通知 system 落库）</b></p>
 *
 * <p>auth 不直连日志库：本记录器把"开始请求 / 开始步骤 / 结束步骤 / 结束请求"四个动作
 * 经 {@link OperationLogFeignClient} 转发给 system-server，落库到
 * sys_user_operation_log / sys_user_operation_step_log。</p>
 *
 * <h3>工作方式</h3>
 * <ul>
 *     <li>被 OperationTraceAspect 调用，业务代码零侵入；</li>
 *     <li>调用树状态保存在 {@link TraceContext}（ThreadLocal 栈）：beginRequest 建根入栈，
 *         beginStep/endStep 负责子步骤压栈/弹栈，endRequest 收尾并清空；</li>
 *     <li>operationMethod / operationIp 从当前 HTTP 请求取（X-Forwarded-For 或 remoteAddr）；
 *         登录为匿名入口，userId 先传 null（成功后如需回填，由业务层把 userId 带出后补传 endRequest）。</li>
 * </ul>
 *
 * <h3>容错原则</h3>
 * <p>记日志是"旁路"，绝不能影响登录业务：所有 Feign 调用 try/catch 降级——
 * 开始失败则本请求放弃记录；结束失败也要保证弹栈/清空上下文。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogRecorder {

    /** 状态：失败 */
    public static final int STATUS_FAIL = 0;
    /** 状态：成功 */
    public static final int STATUS_SUCCESS = 1;

    /** 远端日志落库通道（指向 system-server） */
    private final OperationLogFeignClient operationLogFeignClient;

    /**
     * 开始一次完整请求（@TraceRequest 切面前置阶段调用）
     * <p>建主表行 + 建根步骤（parent=null 即根），随后 TraceContext.begin。
     * 任一步失败则放弃本请求的记录（不建上下文）。</p>
     *
     * @param moduleName 入口模块名，如 auth
     * @param methodName 入口方法名，如 login
     * @param callType   调用方式，如 service
     */
    public void beginRequest(String moduleName, String methodName, String callType) {
        if (TraceContext.hasTrace()) {
            log.warn("操作记录-忽略嵌套的请求入口：{}({})", methodName, moduleName);
            return;
        }
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return;
        }
        String operationMethod = request.getMethod();
        String operationIp = resolveClientIp(request);
        Long logId = null;
        try {
            logId = operationLogFeignClient.startRequest(moduleName, methodName, callType,
                    operationMethod, operationIp, null);
        } catch (Exception e) {
            log.error("操作记录-开始请求失败，本请求放弃记录 {}({})", methodName, moduleName, e);
            return;
        }
        try {
            Long rootStepId = operationLogFeignClient.startStep(logId, null,
                    moduleName, methodName, callType, null, null);
            TraceContext.begin(logId, rootStepId);
            log.info("操作记录-请求开始 log_id={} 根步骤={} {}({})", logId, rootStepId, methodName, moduleName);
        } catch (Exception e) {
            log.error("操作记录-创建根步骤失败 log_id={}", logId, e);
            TraceContext.clear();
        }
    }

    /**
     * 开始一个子步骤（@TraceStep 切面前置阶段调用）
     *
     * @param moduleName 步骤模块名，来自注解 module()
     * @param methodName 步骤方法名，来自被切方法签名
     * @param callType   调用方式，来自注解 callType()
     * @param db         可选：修改的目标库，来自注解 db()
     * @param table      可选：修改的目标表，来自注解 table()
     * @return 本步骤 step_id；无外层请求或记录失败返回 null（调用方应跳过收尾）
     */
    public Long beginStep(String moduleName, String methodName, String callType,
                          String db, String table) {
        if (!TraceContext.hasTrace()) {
            return null; // 无外层请求：孤儿步骤防护
        }
        Long logId = TraceContext.currentLogId();
        Long parentStepId = TraceContext.currentParentStepId();
        try {
            Long stepId = operationLogFeignClient.startStep(logId, parentStepId,
                    moduleName, methodName, callType, blankToNull(db), blankToNull(table));
            if (stepId != null) {
                TraceContext.pushStep(stepId);
            }
            return stepId;
        } catch (Exception e) {
            log.error("操作记录-开始步骤失败 {}({})，父步骤={}", methodName, moduleName, parentStepId, e);
            return null;
        }
    }

    /**
     * 结束一个子步骤（@TraceStep 切面后置/异常阶段调用，与 beginStep 严格配对）
     *
     * @param stepId       beginStep 返回值；null 表示未真正开始，直接返回
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 失败原因，成功为 null
     */
    public void endStep(Long stepId, int status, String errorMessage) {
        if (stepId == null) {
            return;
        }
        TraceContext.popStep(); // 先弹栈，异常路径栈也不会乱
        try {
            operationLogFeignClient.finishStep(TraceContext.currentLogId(), stepId, status, errorMessage);
        } catch (Exception e) {
            log.error("操作记录-结束步骤失败 step_id={}", stepId, e);
        }
    }

    /**
     * 结束一次完整请求（@TraceRequest 切面后置/异常阶段调用）
     *
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 整次请求失败原因，成功为 null
     * @param userId       可选：操作用户 id；为 null 时回退取 TraceContext 里业务暂存的 userId
     *                     （登录入口即在业务中通过 TraceContext.setCurrentUserId 暂存）
     */
    public void endRequest(int status, String errorMessage, Long userId) {
        Long logId = TraceContext.currentLogId();
        if (logId == null) {
            TraceContext.clear();
            return;
        }
        // userId 回填：显式参数优先，否则取业务层暂存在追踪上下文里的操作用户 id
        if (userId == null) {
            userId = TraceContext.currentUserId();
        }
        try {
            Long rootStepId = TraceContext.currentRootStepId();
            if (rootStepId != null) {
                operationLogFeignClient.finishStep(logId, rootStepId, status, errorMessage);
            }
        } catch (Exception e) {
            log.error("操作记录-结束根步骤失败 log_id={}", logId, e);
        }
        try {
            operationLogFeignClient.finishRequest(logId, status, errorMessage, userId);
            log.info("操作记录-请求结束 log_id={} status={}", logId, status);
        } catch (Exception e) {
            log.error("操作记录-结束请求失败 log_id={}", logId, e);
        } finally {
            TraceContext.clear(); // 务必清空，防线程复用串号
        }
    }

    /**
     * 取当前线程绑定的 HttpServletRequest
     *
     * @return 请求对象；非 Web 线程返回 null
     */
    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    /**
     * 解析客户端 IP：优先 X-Forwarded-For（网关透传），否则取 remoteAddr
     *
     * @param request 当前请求
     * @return IP 字符串
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 空串转 null
     *
     * @param value 字符串
     * @return 空白字符串返回 null
     */
    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
