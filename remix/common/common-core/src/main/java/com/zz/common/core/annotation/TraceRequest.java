package com.zz.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p><b>通用核心-请求级操作追踪注解（根节点）</b></p>
 *
 * <p>标注在「一次完整用户操作/请求」的业务入口方法上，例如 auth 的登录 service 方法。</p>
 *
 * <h3>职责</h3>
 * <p>被该方法产生的整棵调用树共用一个 log_id：</p>
 * <ul>
 *     <li>方法进入前 —— 调用 recorder 新建主表记录
 *         {@code sys_user_operation_log}（status=2 执行中），并创建根步骤
 *         {@code sys_user_operation_step_log}（step_no=1、parent_step_id=NULL、status=2）；</li>
 *     <li>方法正常/异常结束后 —— 回写主表 status（1 成功 / 0 失败）、error_message、
 *         并让落库端按 step_no 先序把步骤拼接成 description（如 login(auth)-&gt;getUserInfoByAccount(feign)）；</li>
 *     <li>异常会原样抛出，本注解只负责记录、不吞异常。</li>
 * </ul>
 *
 * <h3>与 @TraceStep 的关系</h3>
 * <p>@TraceRequest 是树的根，@TraceStep 是树上任意一层子节点；
 * 二者由调用方的 OperationTraceAspect 切面配合 TraceContext（ThreadLocal 栈）处理，
 * 栈顶即当前正在执行步骤的 parent_step_id，天然支持嵌套树。</p>
 *
 * <h3>约定</h3>
 * <ul>
 *     <li>同一线程内同一时刻只应存在一个 @TraceRequest 入口；若外层已有跟踪，
 *         建议按普通步骤处理（见切面注释），避免一张主表记录嵌套另一张主表记录；</li>
 *     <li>本注解定义在 common-core 中，是为了让 api 模块的 Feign 接口、各业务模块都能引用；
 *         注解本身不包含任何 Spring/AOP 依赖，切面逻辑在具体服务（如 auth）中实现。</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @TraceRequest(module = "auth", callType = "service")
 * public String login(LoginDTO loginDTO) { ... }
 * }</pre>
 *
 * @since 2026-09-10
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceRequest {

    /**
     * 模块名（即步骤表中 module_name 的值）
     * <p>语义：本方法所属的业务模块/服务，例如 auth、system、gateway；
     * 它与 description 中 "(auth)" 部分对应。</p>
     *
     * @return 模块名
     */
    String module();

    /**
     * 调用方式（即步骤表中 call_type 的值）
     * <p>例如：controller（被 HTTP 请求触发）、service（被本地方法触发）、
     * feign（经 OpenFeign 触发的远程调用）等，可按自己习惯取值。</p>
     *
     * @return 调用方式，默认 controller
     */
    String callType() default "controller";
}
