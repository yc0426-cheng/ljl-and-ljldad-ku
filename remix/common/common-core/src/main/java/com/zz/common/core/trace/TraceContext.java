package com.zz.common.core.trace;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * <p><b>通用核心-操作追踪上下文（线程级，ThreadLocal）</b></p>
 *
 * <p>在一次被 @TraceRequest/@TraceStep 包裹的请求处理期间，保存当前线程的追踪状态：</p>
 * <ul>
 *     <li>logId：整棵调用树归属哪一条 sys_user_operation_log（主表记录）；</li>
 *     <li>rootStepId：本线程内追踪的根（对入口模块 = 根步骤；对跨服务续链模块 = 从请求头恢复的父步骤）；</li>
 *     <li>stepStack：当前执行步骤栈，栈顶 = 正在执行的最深步骤，给子步骤算 parent_step_id。</li>
 * </ul>
 *
 * <h3>生命周期</h3>
 * <ul>
 *     <li>入口模块（如 auth）@TraceRequest 进入：落库端建好主表+根步骤后，{@link #begin(logId, rootStepId)}；</li>
 *     <li>跨服务续链模块（如 system）：过滤器从请求头读到 X-Trace-* 后也调用 {@link #begin}，
 *         此时 rootStepId 传请求头里的父步骤 id，表示"挂到上游那一步下面"；</li>
 *     <li>每进入一个 @TraceStep 方法：{@link #pushStep}；退出：{@link #popStep}（需在 finally 中配对）；</li>
 *     <li>整段处理结束（含异常）：{@link #clear()}，防止线程复用导致串号/内存泄漏。</li>
 * </ul>
 */
public final class TraceContext {

    /**
     * 线程私有追踪状态
     */
    private static final ThreadLocal<TraceHolder> HOLDER = new ThreadLocal<>();

    private TraceContext() {
    }

    /**
     * 每个线程一份的追踪状态
     */
    public static class TraceHolder {

        /**
         * 主表 log_id：整棵调用树归属于哪一条 sys_user_operation_log
         */
        private Long logId;

        /**
         * 本线程追踪的根步骤：入口模块为根步骤 id；跨服务续链模块为请求头中的父步骤 id
         */
        private Long rootStepId;

        /**
         * 当前执行步骤栈，栈顶是正在执行的最深步骤
         */
        private final Deque<Long> stepStack = new ArrayDeque<>();

        /**
         * 本次请求对应的操作用户 id（可空）
         * <p>登录等匿名入口在建主表行时还不知道是谁，业务侧查到用户后
         * 通过 {@link TraceContext#setCurrentUserId} 暂存；请求结束时
         * 记录器读取它并随 finishRequest 回填主表 user_id。</p>
         */
        private Long userId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getLogId() {
            return logId;
        }

        public void setLogId(Long logId) {
            this.logId = logId;
        }

        public Long getRootStepId() {
            return rootStepId;
        }

        public void setRootStepId(Long rootStepId) {
            this.rootStepId = rootStepId;
        }

        /**
         * 取栈顶步骤（当前正在执行的步骤），空栈返回 null
         *
         * @return 栈顶 step_id 或 null
         */
        public Long peekStep() {
            return stepStack.peek();
        }

        public void pushStep(Long stepId) {
            stepStack.push(stepId);
        }

        public Long popStep() {
            return stepStack.pop();
        }
    }

    /**
     * 是否已有外层追踪（切面据此判断：无追踪时 @TraceStep 直接放行、@TraceRequest 才建根）
     *
     * @return true = 当前线程已处于一次请求追踪中
     */
    public static boolean hasTrace() {
        TraceHolder holder = HOLDER.get();
        return holder != null && holder.getLogId() != null;
    }

    /**
     * 开启追踪：写入 log_id 与根步骤并压栈
     *
     * @param logId       主表 log_id
     * @param rootStepId  根步骤 id（入口模块 = 落库端建的根步骤；续链模块 = 请求头里的父步骤 id）
     */
    public static void begin(Long logId, Long rootStepId) {
        clear();
        TraceHolder holder = new TraceHolder();
        holder.setLogId(logId);
        holder.setRootStepId(rootStepId);
        if (rootStepId != null) {
            holder.pushStep(rootStepId);
        }
        HOLDER.set(holder);
    }

    /**
     * 获取当前追踪的 log_id
     *
     * @return log_id；无追踪时返回 null
     */
    public static Long currentLogId() {
        TraceHolder holder = HOLDER.get();
        return holder == null ? null : holder.getLogId();
    }

    /**
     * 暂存本次请求的操作用户 id（供请求结束时回填主表 user_id）
     * <p>登录等匿名入口在业务中途才得知用户身份，调用本方法写入；
     * 无追踪上下文时静默忽略（记日志失败/未启用时不影响业务）。</p>
     *
     * @param userId 操作用户 id，可为 null（清除）
     */
    public static void setCurrentUserId(Long userId) {
        TraceHolder holder = HOLDER.get();
        if (holder != null) {
            holder.setUserId(userId);
        }
    }

    /**
     * 获取本次请求的操作用户 id
     *
     * @return userId；未设置或未在追踪中返回 null
     */
    public static Long currentUserId() {
        TraceHolder holder = HOLDER.get();
        return holder == null ? null : holder.getUserId();
    }

    /**
     * 获取当前追踪的根步骤（入口 = 根步骤；续链 = 上游父步骤）
     *
     * @return 根 step_id；无追踪时返回 null
     */
    public static Long currentRootStepId() {
        TraceHolder holder = HOLDER.get();
        return holder == null ? null : holder.getRootStepId();
    }

    /**
     * 获取当前正在执行步骤的 step_id（作为下一个子步骤的 parent_step_id）
     *
     * @return 栈顶 step_id；无追踪或空栈返回 null
     */
    public static Long currentParentStepId() {
        TraceHolder holder = HOLDER.get();
        return holder == null ? null : holder.peekStep();
    }

    /**
     * 子步骤开始：把新 step_id 压栈（进入更深一层）
     *
     * @param stepId 落库端返回的步骤 step_id
     */
    public static void pushStep(Long stepId) {
        TraceHolder holder = HOLDER.get();
        if (holder != null) {
            holder.pushStep(stepId);
        }
    }

    /**
     * 子步骤结束：弹出本层步骤
     * <p>必须与 pushStep 严格配对（异常也要弹，建议放 finally），避免栈错乱。</p>
     */
    public static void popStep() {
        TraceHolder holder = HOLDER.get();
        if (holder != null && !holder.stepStack.isEmpty()) {
            holder.stepStack.pop();
        }
    }

    /**
     * 结束并清空本次追踪状态
     * <p>入口模块整次请求结束时调用；续链模块在过滤器 finally 中调用。</p>
     */
    public static void clear() {
        HOLDER.remove();
    }
}
