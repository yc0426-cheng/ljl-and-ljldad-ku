package com.yc.api.system.dto

import com.yc.api.system.enums.SysUserStatusEnum
import java.util.Date

data class SysUserFeignDTO(
    var userId: Long = 0,
    var account: String = "",
    var name: String = "",
    var password: String = "",
    var phone: String = "",
    var idNumber: String = "",
    var email: String = "",
    var passErrorCount: Int = 0,
    var status: SysUserStatusEnum = SysUserStatusEnum.ENABLE,
    var lastLoginTime: Date? = null,
    var delFlag: Boolean = false
)
