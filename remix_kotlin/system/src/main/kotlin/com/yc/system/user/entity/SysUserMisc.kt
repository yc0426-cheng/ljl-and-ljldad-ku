package com.yc.system.user.entity

import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.yc.common.core.entity.BaseEntity

@TableName("sys_user_misc")
class SysUserMisc : BaseEntity() {
    @TableId
    var miscId: String = ""
    var userId: String = ""
    var avatar: String = ""
    var nickname: String = ""
    var signature: String = ""
    var historyAvatars: String = ""
}
