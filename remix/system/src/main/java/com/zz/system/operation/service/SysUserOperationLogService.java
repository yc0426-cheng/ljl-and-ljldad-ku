package com.zz.system.operation.service;

/**
 * <p><b>系统服务-用户操作记录业务接口</b></p>
 *
 * <p>用户操作日志（主表 sys_user_operation_log + 子表 sys_user_operation_step_log）
 * 的统一落库入口。本接口的四个方法与 Feign 客户端 OperationLogFeignClient 的四个动作一一对应，
 * 供其他模块（auth 等）通过 HTTP 调用；System 内部若也要记录自身操作可直接注入本接口。</p>
 *
 * <h3>实现要点（对应四个动作，详见 impl 注释）</h3>
 * <ol>
 *     <li>startRequest：新建主表记录（status=2）+ 记录 operation_date；返回 log_id；</li>
 *     <li>startStep：分配 step_no = 该 log 下现有最大 step_no + 1；
 *         parentStepId 为空且尚无根 → 建根步骤；否则以 parentStepId 为父；status=2、start_time=now；</li>
 *     <li>finishStep：回写 status / error_message / end_time，cost_ms = end - start；</li>
 *     <li>finishRequest：回写主表 status / error_message / 可选 userId，
 *         并按步骤表 step_no 先序拼接 description（method(module) 以 -&gt; 连接）。</li>
 * </ol>
 *
 * @since 2026-09-10
 */
public interface SysUserOperationLogService {

    /**
     * 开始记录一次请求：建主表行（执行中）
     *
     * @param moduleName      入口方法所属模块，如 auth
     * @param methodName      入口方法名，如 login
     * @param callType        调用方式，如 controller / service
     * @param operationMethod HTTP 请求方法，可为 null
     * @param operationIp     客户端 IP，可为 null
     * @param userId          操作用户 id，可为 null（登录后可在 finishRequest 回填）
     * @return 主表 log_id
     */
    Long startRequest(String moduleName, String methodName, String callType,
                      String operationMethod, String operationIp, Long userId);

    /**
     * 开始一个步骤：建步骤行（执行中）
     *
     * @param logId        归属请求 log_id
     * @param parentStepId 父步骤 step_id；null 表示根步骤（该 log 尚无根时自动建根）
     * @param moduleName   本步骤模块名
     * @param methodName   本步骤方法名
     * @param callType     调用方式
     * @param targetDb     可选：本步骤修改的目标库名（如 learn），只读传 null
     * @param targetTable  可选：本步骤修改的目标表名（如 sys_user），只读传 null
     * @return 步骤 step_id
     */
    Long startStep(Long logId, Long parentStepId, String moduleName, String methodName, String callType,
                   String targetDb, String targetTable);

    /**
     * 结束一个步骤：回写结果与耗时
     *
     * @param logId        归属请求 log_id
     * @param stepId       步骤 step_id
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 失败原因，成功为 null
     */
    void finishStep(Long logId, Long stepId, Integer status, String errorMessage);

    /**
     * 结束一次请求：回写主表最终结果并生成 description
     *
     * @param logId        主表 log_id
     * @param status       0 = 失败，1 = 成功
     * @param errorMessage 整次请求失败原因，成功为 null
     * @param userId       可选回填的操作用户 id（登录成功后传入），不需要传 null
     */
    void finishRequest(Long logId, Integer status, String errorMessage, Long userId);
}
