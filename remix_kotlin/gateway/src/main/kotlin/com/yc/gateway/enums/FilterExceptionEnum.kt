package com.yc.gateway.enums

import com.yc.common.core.enums.error.AbstractBaseExceptionEnum

/**
 * <p><b>网关服务-过滤器异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/18 21:24
 */
enum class FilterExceptionEnum(
    val code: Int,
    override val errorMessage: String,
) : AbstractBaseExceptionEnum {
    TOKEN_NOT_EXISTS(1, "token不存在"),
    USER_NOT_EXISTS(2, "用户信息不存在");

    override val errorCode: String
        get() = code.toString()
}