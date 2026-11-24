package com.yc.api.system.enums

import com.baomidou.mybatisplus.annotation.EnumValue

/**
 * <p><b>系统服务-系统用户状态枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 17:17
 */
enum class SysUserStatusEnum(
    @EnumValue
    val value: Int,
    val desc: String?
) {
    ENABLE(1, "启用"),
    FROZEN(0, "冻结"),
    DORMANT(-1, "休眠")
}
