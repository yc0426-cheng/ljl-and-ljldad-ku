package com.yc.system.user.pojo.vo

import com.yc.system.user.enums.SysUserStatusEnum
import java.util.Date

data class SysUserVO(
    val userId: Long,
    val account: String,
    val name: String,
    val password: String,
    val phone: String,
    val idNumber: String,
    val email: String,
    val passErrorCount: Int,
    val status: SysUserStatusEnum,
    val lastLoginTime: Date?,
    val delFlag: Boolean
)
