package com.yc.system.menu.entity

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.annotation.TableName
import com.yc.common.core.entity.BaseEntity

/**
 * 系统菜单表（sys_menu）
 *
 * @TableName sys_menu
 * @author yangcheng
 * @since 2026-11-19
 */
@TableName("sys_menu")
class SysMenu : BaseEntity() {
    @TableId
    var menuId: Long = 0
    var parentId: Long? = null
    var menuName: String = ""
    var menuType: Int? = null
    var routePath: String = ""
    var component: String = ""
    var icon: String = ""
    var orderNo: Int = 0
    var visible: Boolean = true
    var status: Int = 1
    @TableLogic
    var delFlag: Boolean = false
    @TableField(exist = false)
    var children: MutableList<SysMenu> = ArrayList()
}
