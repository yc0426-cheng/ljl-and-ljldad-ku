package com.yc.system.role.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.annotation.TableName
import com.yc.common.core.entity.BaseEntity

/**
 * 系统角色表
 *
 * @author yangcheng
 * @since 2026-09-23 22:29:16
 */
@TableName("sys_user")
class SysRole : BaseEntity() {
    @TableId
    var roleId: Long = 0
    var roleName: String = ""
    var enabled: Int = 0
    @TableLogic
    var delFlag: Int = 0
}
