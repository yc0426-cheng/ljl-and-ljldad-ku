package com.yc.system.user.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.yc.common.core.entity.BaseEntity
import com.yc.system.user.enums.SysUserStatusEnum
import java.util.Date

/**
 * <p><b>系统服务-系统用户状态枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/16 19:51
 */
@TableName("sys_user")
class SysUser : BaseEntity() {
    @TableId
    var userId: Long = 0
    var account: String = ""
    var name: String = ""
    var password: String = ""
    var phone: String = ""
    var idNumber: String = ""
    var email: String = ""
    var passErrorCount: Int = 0
    var status: SysUserStatusEnum = SysUserStatusEnum.ENABLE
    var lastLoginTime: Date? = null
    var delFlag: Boolean = false
}
