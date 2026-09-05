package com.zz.system.operation.controller;

import com.zz.system.operation.service.SysUserOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>系统服务-用户操作记录控制器（日志落库对外接口）</b></p>
 *
 * <p>供 OperationLogFeignClient（auth 等模块使用）调用的四个落库端点，
 * 仅做参数透传 + 调用 Service，真正落库逻辑见 SysUserOperationLogServiceImpl。</p>
 *
 * <p>路径与 Feign 客户端 @FeignClient(path = "/sys/operation/log") 一致。
 * 本控制器<b>不要</b>标注任何追踪注解——它是日志通道本身，不能被再次追踪。</p>
 *
 * <h3>四个端点</h3>
 * <ul>
 *     <li>POST /sys/operation/log/start —— 建主表行，返回 log_id；</li>
 *     <li>POST /sys/operation/log/step/start —— 建步骤行，返回 step_id；</li>
 *     <li>POST /sys/operation/log/step/finish —— 回写步骤结果与耗时；</li>
 *     <li>POST /sys/operation/log/finish —— 回写主表结果并生成 description。</li>
 * </ul>
 *
 * @since 2026-09-10
 */
@RestController
@RequiredArgsConstructor
public class SysUserOperationLogController {

    private final SysUserOperationLogService operationLogService;

    /**
     * 开始记录一次请求（建主表行，status=2 执行中）
     *
     * @param moduleName      入口模块名，如 auth
     * @param methodName      入口方法名，如 login
     * @param callType        调用方式，可为 null
     * @param operationMethod HTTP 方法，可为 null
     * @param operationIp     客户端 IP，可为 null
     * @param userId          操作用户 id，可为 null
     * @return 主表 log_id
     */
    @PostMapping("/sys/operation/log/start")
    public Long startRequest(@RequestParam("moduleName") String moduleName,
                             @RequestParam("methodName") String methodName,
                             @RequestParam(value = "callType", required = false) String callType,
                             @RequestParam(value = "operationMethod", required = false) String operationMethod,
                             @RequestParam(value = "operationIp", required = false) String operationIp,
                             @RequestParam(value = "userId", required = false) Long userId) {
        return operationLogService.startRequest(moduleName, methodName, callType,
                operationMethod, operationIp, userId);
    }

    /**
     * 开始一个步骤（建步骤行，status=2 执行中）
     *
     * @param logId        归属请求 log_id
     * @param parentStepId 父步骤 step_id；null 表示根步骤（无根自动建根）
     * @param moduleName   本步骤模块名
     * @param methodName   本步骤方法名
     * @param callType     调用方式
     * @param targetDb     可选：修改的目标库名
     * @param targetTable  可选：修改的目标表名
     * @return 步骤 step_id
     */
    @PostMapping("/sys/operation/log/step/start")
    public Long startStep(@RequestParam("logId") Long logId,
                          @RequestParam(value = "parentStepId", required = false) Long parentStepId,
                          @RequestParam("moduleName") String moduleName,
                          @RequestParam("methodName") String methodName,
                          @RequestParam(value = "callType", required = false) String callType,
                          @RequestParam(value = "targetDb", required = false) String targetDb,
                          @RequestParam(value = "targetTable", required = false) String targetTable) {
        return operationLogService.startStep(logId, parentStepId, moduleName, methodName,
                callType, targetDb, targetTable);
    }

    /**
     * 结束一个步骤（回写 status/error/耗时）
     *
     * @param logId        归属请求 log_id
     * @param stepId       步骤 step_id
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 失败原因，可为 null
     */
    @PostMapping("/sys/operation/log/step/finish")
    public void finishStep(@RequestParam("logId") Long logId,
                           @RequestParam("stepId") Long stepId,
                           @RequestParam("status") Integer status,
                           @RequestParam(value = "errorMessage", required = false) String errorMessage) {
        operationLogService.finishStep(logId, stepId, status, errorMessage);
    }

    /**
     * 结束一次请求（回写主表最终结果并生成 description）
     *
     * @param logId        主表 log_id
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 整次请求失败原因，可为 null
     * @param userId       可选回填用户 id，可为 null
     */
    @PostMapping("/sys/operation/log/finish")
    public void finishRequest(@RequestParam("logId") Long logId,
                              @RequestParam("status") Integer status,
                              @RequestParam(value = "errorMessage", required = false) String errorMessage,
                              @RequestParam(value = "userId", required = false) Long userId) {
        operationLogService.finishRequest(logId, status, errorMessage, userId);
    }
}
