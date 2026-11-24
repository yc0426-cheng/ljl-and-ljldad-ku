package com.yc.system.menu.enums

import com.yc.common.core.enums.error.AbstractBaseExceptionEnum

/**
 * <p><b>系统服务-系统菜单异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026.11.19
 */
enum class SysMenuExceptionEnum(
    code: Int,
    override val errorMessage: String
) : AbstractBaseExceptionEnum {
    ROLE_NAME_IS_NOT_EMPTY(1, "菜单名称不能为空"),
    ROUTE_IS_EMPTY(2, "菜单(叶子)必须填写路由地址"),
    MENU_IS_NOT_EXIST_OR_DELETED(3, "菜单不存在或已删除")
    ;

    override val errorCode: String = code.toString()
}