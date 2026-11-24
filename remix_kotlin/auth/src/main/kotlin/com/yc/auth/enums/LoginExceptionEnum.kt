package com.yc.auth.enums

import com.yc.common.core.enums.error.AbstractBaseExceptionEnum

enum class LoginExceptionEnum(
    val code: Int,
    override val errorMessage: String
) : AbstractBaseExceptionEnum {
    USER_LOCKED(1, "账号已被锁定，请联系管理员解锁"),
    PASSWORD_ERROR(2, "密码错误"),
    TOKEN_INVALID(3, "登录已失效，请重新登录");

    override val errorCode: String
        get() = code.toString()
}