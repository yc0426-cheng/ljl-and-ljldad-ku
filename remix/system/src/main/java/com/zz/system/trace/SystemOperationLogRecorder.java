package com.zz.system.trace;

import com.zz.common.core.trace.TraceContext;
import com.zz.system.operation.service.SysUserOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * <p><b>系统服务-操作日志记录器（本地落库）</b></p>
 *
 * <p>auth 走 Feign 通知 system 落库；而 system 自己就在落库方，直接注入
 * {@link SysUserOperationLogService} 本地写，不再经 HTTP 自调用。</p>
 *
 * <p>上下文与 auth 的 OperationLogRecorder 语义一致：</p>
 * <ul>
 *     <li>TraceHeaderSeedFilter 已从请求头恢复 logId + 父步骤（跨服务续链），
 *         @TraceStep 步骤直接挂到上游父步骤之下，<b>不建主表</b>；</li>
 *     <li>若本模块未来有网关直达入口（无请求头），入口方法加 @TraceRequest 后走
 *         beginRequest/endRequest 建主表分支。</li>
 * </ul>
 *
 * <p>容错原则与 auth 相同：记录失败绝不影响业务，所有写库调用 try/catch 降级。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemOperationLogRecorder {

    /** 状态：失败 */
    public static final int STATUS_FAIL = 0;
    /** 状态：成功 */
    public static final int STATUS_SUCCESS = 1;

    /** 本地落库 Service */
    private final SysUserOperationLogService operationLogService;

    /**
     * 开始一次完整请求（网关直达入口，@TraceRequest 使用；当前 system 暂无此入口，预留）
     *
     * <p>实现步骤：</p>
     * <ol>
     *     <li>已存在追踪上下文则直接返回（防嵌套）；</li>
     *     <li>operationLogService.startRequest(...) 建主表行 → logId；</li>
     *     <li>operationLogService.startStep(logId, null, module, method, callType, null, null)
     *         建根步骤 → rootStepId（parent 为 null = 根）；</li>
     *     <li>TraceContext.begin(logId, rootStepId)。</li>
     * </ol>
     */
    public void beginRequest(String moduleName, String methodName, String callType) {
        if (TraceContext.hasTrace()) {
            return;
        }
        try {
            Long logId = operationLogService.startRequest(moduleName, methodName, callType,
                    null, null, null);
            Long rootStepId = operationLogService.startStep(logId, null, moduleName,
                    methodName, callType, null, null);
            TraceContext.begin(logId, rootStepId);
        } catch (Exception e) {
            log.error("操作记录-本模块开始请求失败，放弃记录本请求", e);
            TraceContext.clear();
        }
    }

    /**
     * 开始一个子步骤（@TraceStep 使用；续链场景下步骤挂在请求头父步骤之下）
     *
     * @param moduleName 步骤模块名
     * @param methodName 步骤方法名
     * @param callType   调用方式
     * @param db         可选：修改的目标库
     * @param table      可选：修改的目标表
     * @return 本步骤 step_id；无上下文返回 null（调用方应跳过收尾）
     */
    public Long beginStep(String moduleName, String methodName, String callType,
                          String db, String table) {
        if (!TraceContext.hasTrace()) {
            return null;
        }
        Long logId = TraceContext.currentLogId();
        Long parentStepId = TraceContext.currentParentStepId();
        try {
            Long stepId = operationLogService.startStep(logId, parentStepId, moduleName,
                    methodName, callType, blankToNull(db), blankToNull(table));
            if (stepId != null) {
                TraceContext.pushStep(stepId);
            }
            return stepId;
        } catch (Exception e) {
            log.error("操作记录-开始步骤失败 module={} method={}", moduleName, methodName, e);
            return null;
        }
    }

    /**
     * 结束一个子步骤（必须与 beginStep 配对，弹栈放最前保证异常路径栈不乱）
     *
     * @param stepId       beginStep 返回值；null 表示未真正开始，直接返回
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 失败原因，成功为 null
     */
    public void endStep(Long stepId, int status, String errorMessage) {
        if (stepId == null) {
            return;
        }
        TraceContext.popStep();
        try {
            operationLogService.finishStep(TraceContext.currentLogId(), stepId, status, errorMessage);
        } catch (Exception e) {
            log.error("操作记录-结束步骤失败 step_id={}", stepId, e);
        }
    }

    /**
     * 结束一次完整请求（@TraceRequest 收尾；续链场景不会走到本方法）
     *
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 失败原因，成功为 null
     * @param userId       可选：操作用户 id；为 null 时回退取 TraceContext 里业务暂存的 userId
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
                operationLogService.finishStep(logId, rootStepId, status, errorMessage);
            }
        } catch (Exception e) {
            log.error("操作记录-结束根步骤失败 log_id={}", logId, e);
        }
        try {
            operationLogService.finishRequest(logId, status, errorMessage, userId);
        } catch (Exception e) {
            log.error("操作记录-结束请求失败 log_id={}", logId, e);
        } finally {
            TraceContext.clear();
        }
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
