package com.yc.system.role.enums

import com.yc.common.core.enums.error.AbstractBaseExceptionEnum

/**
 * <p><b>系统服务-角色异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/19 18:12
 */
enum class SysRoleExceptionEnum(
    val code: Int,
    override val errorMessage: String
): AbstractBaseExceptionEnum {
    ROLE_NAME_IS_ALREADY_EXISTS(1, "角色名称已存在"),
    IS_NOT_EDIT(2, "角色未进行修改");

    override val errorCode: String = code.toString()
}