package com.yc.system.userRole.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.yc.common.core.entity.BaseEntity

/**
 * 用户角色绑定表
 *
 * @author yangcheng
 * @since 2026-11-19 20:02:28
 */
@TableName("sys_user_role")
class SysUserRole : BaseEntity() {
    @TableId
    var userRoleId: Long = 0
    var userId: Long = 0
    var roleId: Long = 0
}
