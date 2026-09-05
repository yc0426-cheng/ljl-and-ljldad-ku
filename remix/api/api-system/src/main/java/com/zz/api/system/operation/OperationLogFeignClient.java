package com.zz.api.system.operation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p><b>远端调用-用户操作日志记录接口（落库方为 system-server）</b></p>
 *
 * <p>按约定「日志表 + 子表由 system 模块统一落库」：任何模块（auth / system / 未来新增模块）
 * 想要记录"一次请求 + 其方法调用链"，都通过本客户端通知 system-server 写入
 * {@code sys_user_operation_log} 与 {@code sys_user_operation_step_log} 两张表，
 * 业务模块自身不直连日志库、不重复建实体/Mapper。</p>
 *
 * <h3>接口协议（四个动作，对应一棵追踪树的完整生命周期）</h3>
 * <ol>
 *     <li>{@link #startRequest} —— 创建主表记录（status=2 执行中），返回 log_id；</li>
 *     <li>{@link #startStep} —— 创建一条步骤记录（status=2 执行中），返回 step_id。
 *         <ul>
 *             <li>parentStepId = null 且该 log 尚无根步骤时 → 落库端自动建根步骤（step_no=1、parent 为空）；</li>
 *             <li>parentStepId = 栈顶 step_id → 作为该步骤的子步骤（嵌套树）。</li>
 *         </ul>
 *     </li>
 *     <li>{@link #finishStep} —— 回写步骤结果（status / error_message / end_time / cost_ms）；</li>
 *     <li>{@link #finishRequest} —— 回写主表最终结果（status / error_message，可选回填 user_id），
 *         并由落库端按 step_no 先序把步骤拼成 description（如 login(auth)-&gt;getUserInfoByAccount(feign)）。</li>
 * </ol>
 *
 * <h3>重要约定</h3>
 * <ul>
 *     <li><b>本客户端方法不要标注任何追踪注解</b>：它是"记日志的通道"，若再被追踪会在记录日志时
 *         又产生一条日志（无限递归）。</li>
 *     <li>调用方（auth 的记录器）对日志接口的失败必须 try/catch 吞掉：记日志失败不能影响登录等业务。</li>
 *     <li>status 取值与 SQL 注释一致：主表/步骤 0=失败、1=成功；步骤额外支持 2=执行中（一般只在落库端内部用）。</li>
 * </ul>
 *
 * <p>路径风格与 {@code SysUserFeignClient} 保持一致：@FeignClient name 用注册中心服务名，
 * path 为系统侧 Controller 的公共前缀。</p>
 *
 * @author yangcheng
 * @since 2026-09-10
 */
@FeignClient(name = "system-server", contextId = "OperationLogFeignClient", path = "/sys/operation/log")
public interface OperationLogFeignClient {

    /**
     * 开始记录一次完整请求：新建主表 sys_user_operation_log（status=2 执行中）
     *
     * @param moduleName      入口方法所属模块，如 auth（将作为根步骤的 module_name）
     * @param methodName      入口方法名，如 login（将作为根步骤的 method_name）
     * @param callType        入口调用方式，如 controller / service
     * @param operationMethod 当前 HTTP 请求方法（GET/POST/...），可为 null（对应主表 operation_method）
     * @param operationIp     客户端 IP，可为 null（对应主表 operation_ip）
     * @param userId          操作用户 id；登录等匿名场景传 null，成功后可在 finishRequest 回填
     * @return 主表 log_id
     */
    @PostMapping("/start")
    Long startRequest(@RequestParam("moduleName") String moduleName,
                      @RequestParam("methodName") String methodName,
                      @RequestParam(value = "callType", required = false) String callType,
                      @RequestParam(value = "operationMethod", required = false) String operationMethod,
                      @RequestParam(value = "operationIp", required = false) String operationIp,
                      @RequestParam(value = "userId", required = false) Long userId);

    /**
     * 开始一个方法步骤：新建 sys_user_operation_step_log（status=2 执行中）
     *
     * @param logId        归属的请求 log_id（必须来自 startRequest 返回值）
     * @param parentStepId 父步骤 step_id；null 表示根步骤（该 log 尚无根时自动建根）
     * @param moduleName   本步骤模块名（如 feign / system）
     * @param methodName   本步骤方法名（如 getUserInfoByAccount）
     * @param callType     调用方式（如 feign / local / service）
     * @param targetDb     可选：本步骤修改的目标数据库名（如 learn），只读步骤传 null
     * @param targetTable  可选：本步骤修改的目标表名（如 sys_user），只读步骤传 null
     * @return 本步骤 step_id
     */
    @PostMapping("/step/start")
    Long startStep(@RequestParam("logId") Long logId,
                   @RequestParam(value = "parentStepId", required = false) Long parentStepId,
                   @RequestParam("moduleName") String moduleName,
                   @RequestParam("methodName") String methodName,
                   @RequestParam(value = "callType", required = false) String callType,
                   @RequestParam(value = "targetDb", required = false) String targetDb,
                   @RequestParam(value = "targetTable", required = false) String targetTable);

    /**
     * 结束一个方法步骤：回写 status / error_message，并计算 end_time、cost_ms
     *
     * @param logId        归属的请求 log_id
     * @param stepId       本步骤 step_id（来自 startStep 返回值）
     * @param status       0=失败，1=成功
     * @param errorMessage 失败原因，成功时为 null
     */
    @PostMapping("/step/finish")
    void finishStep(@RequestParam("logId") Long logId,
                    @RequestParam("stepId") Long stepId,
                    @RequestParam("status") Integer status,
                    @RequestParam(value = "errorMessage", required = false) String errorMessage);

    /**
     * 结束一次完整请求：回写主表 status / error_message / description，可选回填 user_id
     *
     * @param logId        主表 log_id
     * @param status       0=失败，1=成功
     * @param errorMessage 整次请求失败原因，成功时为 null
     * @param userId       可选：登录成功后补填操作用户 id；不需要时传 null
     */
    @PostMapping("/finish")
    void finishRequest(@RequestParam("logId") Long logId,
                       @RequestParam("status") Integer status,
                       @RequestParam(value = "errorMessage", required = false) String errorMessage,
                       @RequestParam(value = "userId", required = false) Long userId);
}
