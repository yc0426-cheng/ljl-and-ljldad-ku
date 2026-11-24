package com.yc.system.user.enums

import com.yc.common.core.enums.error.AbstractBaseExceptionEnum

enum class SysUserExceptionEnum(
    private val code: Int,
    override val errorMessage: String
) : AbstractBaseExceptionEnum {
    OUT_OF_ERROR_COUNT(1, "密码错误三次,请等待5分钟"),
    SYS_USER_NOT_NULL(2, "用户对象不存在");

    override val errorCode: String
        get() = code.toString()
}