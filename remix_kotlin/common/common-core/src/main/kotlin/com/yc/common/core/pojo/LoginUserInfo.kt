package com.yc.common.core.pojo

/**
 * <p><b>通用工具-登录用户信息 POJO类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/16 20:38
 */
data class LoginUserInfo(
    val userId: Long = 0,
    val account: String = "",
    val name: String = "",
    val phone: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    val signature: String? = null,
    val avatar: String? = null,
    var token: String? = null
)
