package com.zz.common.core.context;

import com.zz.common.core.pojo.LoginUserInfo;

/**
 * <p><b>核心工具类-全局存储用户信息threadLocal工具类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/21 21:31
 */
public class LoginUserHolder {

    private static final ThreadLocal<LoginUserInfo> USER_HOLDER = new ThreadLocal<>();

    /**
     * 存储用户信息到当前线程上下文
     *
     * @param loginUserInfo 登录用户信息
     */
    public static void set(LoginUserInfo loginUserInfo) {
        USER_HOLDER.set(loginUserInfo);
    }

    /**
     * 获取当前线程上下文中的用户信息
     *
     * @return 登录用户信息，可能为null
     */
    public static LoginUserInfo get() {
        return USER_HOLDER.get();
    }

    /**
     * 移除当前线程上下文中的用户信息，防止内存泄漏
     */
    public static void remove() {
        USER_HOLDER.remove();
    }
}
