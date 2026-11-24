package com.yc.system.role.pojo.vo

import java.util.Date

/**
 * <p><b>系统服务-角色返回结果类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/19 18:24
 */
data class SysRoleVO(
    val roleId: Long,
    val roleName: String,
    val enabled: Int,
    val delFlag: Int,
    val createUser: Long,
    val createTime: Date,
    val updateUser: Long,
    val updateTime: Date
)