package com.zz.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p><b>通用核心-方法级步骤追踪注解（树节点）</b></p>
 *
 * <p>标注在需要记录为「调用链中一步」的方法上，与 @TraceRequest 配套使用。</p>
 *
 * <h3>职责</h3>
 * <ul>
 *     <li>方法进入前 —— 以当前线程 TraceContext 中 log_id 为归属、栈顶步骤为 parent_step_id，
 *         创建一条步骤记录 {@code sys_user_operation_step_log}（status=2 执行中，step_no 由落库端递增分配），
 *         并把新 step_id 压入线程栈（表示进入更深一层）；</li>
 *     <li>方法正常/异常结束后 —— 回写该步骤 status（1 成功 / 0 失败）、error_message，
 *         计算 end_time / cost_ms，随后弹出栈（表示回到上一层）；</li>
 *     <li>若当前线程不存在 TraceContext（没有外层 @TraceRequest），本注解应视为无效（直接放行），
 *         避免出现孤儿步骤——这与 @TraceRequest 是"根"的语义一致。</li>
 * </ul>
 *
 * <h3>用在 Feign 方法上时的注意事项</h3>
 * <p>OpenFeign 客户端是代理 Bean，Spring AOP 对其方法标注的切面在部分版本/代理顺序下可能不生效
 * （参见社区反馈：AOP pointcut is not working with Feign client）。
 * 若 @TraceStep 直接标在 Feign 接口方法上自测不生效，推荐改用「门面/包装」方式：
 * 在调用模块内新增一个被 Spring 管理的包装组件（如 SysUserFeignFacade），
 * 方法体里只做一件事——调用对应 Feign 方法，并把 @TraceStep 标在包装方法上，
 * 这样切面作用的是普通 Spring Bean，稳定可靠。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @TraceStep(module = "feign", callType = "feign")
 * SysUserFeignDTO getUserInfoByAccount(@RequestParam("account") String account);
 * }</pre>
 *
 * @since 2026-09-10
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceStep {

    /**
     * 模块名（步骤表中 module_name 的值）
     * <p>语义同 @TraceRequest.module；对 Feign 调用可取被调端服务名（如 system），
     * 也可以沿用你习惯的 feign 写法，保持 description 风格统一即可。</p>
     *
     * @return 模块名
     */
    String module();

    /**
     * 调用方式（步骤表中 call_type 的值）
     *
     * @return 调用方式，默认 local
     */
    String callType() default "local";

    /**
     * （可选）本步骤修改的目标数据库名
     * <p>仅当本方法会写数据时填写（如 db = "learn"），只读方法留空即可；
     * 非空时写入步骤行的 target_db，用于回答"改了哪个库"，不记录具体 SQL。</p>
     *
     * @return 目标库名，默认空
     */
    String db() default "";

    /**
     * （可选）本步骤修改的目标表名
     * <p>仅当本方法会写数据时填写（如 table = "sys_user"），只读方法留空即可；
     * 非空时写入步骤行的 target_table。</p>
     *
     * @return 目标表名，默认空
     */
    String table() default "";
}
