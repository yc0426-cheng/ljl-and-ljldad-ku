package com.yc.system.user.pojo.dto

import com.yc.system.user.enums.SysUserStatusEnum

/**
 * <p><b>系统服务-系统用户查询类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/16 20:43
 */
data class SysUserDTO(
    val userId: Long,
    val account: String,
    val name: String,
    val phone: String,
    val idNumber: String,
    val email: String,
    val status: SysUserStatusEnum
)
