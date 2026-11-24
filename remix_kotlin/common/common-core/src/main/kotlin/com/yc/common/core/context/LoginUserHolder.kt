package com.yc.common.core.context

import com.yc.common.core.pojo.LoginUserInfo

/**
 * <p><b>核心工具类-全局存储用户信息threadLocal工具类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/17 11:13
 */
object LoginUserHolder {

    private val USER_HOLDER = ThreadLocal<LoginUserInfo>()

    /**
     * 存储用户信息到当前线程上下文
     *
     * @param loginUserInfo 登录用户信息
     */
    fun set(loginUserInfo: LoginUserInfo) = USER_HOLDER.set(loginUserInfo)

    /**
     * 获取当前线程上下文中的用户信息
     *
     * @return 登录用户信息，可能为null
     */
    fun get(): LoginUserInfo? = USER_HOLDER.get()

    /**
     * 移除当前线程上下文中的用户信息，防止内存泄漏
     */
    fun remove() = USER_HOLDER.remove()
}