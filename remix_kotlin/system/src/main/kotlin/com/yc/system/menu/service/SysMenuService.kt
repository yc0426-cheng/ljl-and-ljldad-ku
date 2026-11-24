package com.yc.system.menu.service

import com.baomidou.mybatisplus.extension.service.IService
import com.yc.system.menu.entity.SysMenu

/**
 * 系统菜单业务接口
 *
 * @description 针对表【sys_menu(系统菜单表)】的数据库操作Service
 * @author yangcheng
 * @since 2026-11-19
 */
interface SysMenuService : IService<SysMenu> {

    /**
     * 查询菜单树（未删除且启用状态，按 order_no/menu_id 升序，含 children 组装）
     *
     * @return 顶级菜单列表（内含嵌套 children）
     */
    fun listTree(): List<SysMenu>

    /**
     * 新增菜单
     *
     * @param menu 菜单（menuId 可空，雪花自动生成；默认值见实现）
     * @return 新菜单 ID
     */
    fun addMenu(menu: SysMenu): Long

    /**
     * 编辑菜单
     *
     * @param menu 菜单（menuId 必填）
     */
    fun editMenu(menu: SysMenu)

    /**
     * 删除菜单（软删除：自身 + 全部子孙）
     *
     * @param menuId 菜单 ID
     */
    fun deleteMenu(menuId: Long)
}