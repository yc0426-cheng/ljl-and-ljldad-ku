package com.yc.system.menu.controller

import com.yc.common.core.anotation.GetResource
import com.yc.common.core.anotation.PostResource
import com.yc.common.core.anotation.RestResource
import com.yc.system.menu.entity.SysMenu
import com.yc.system.menu.service.SysMenuService
import org.springframework.web.bind.annotation.RequestBody

/**
 * <p><b>系统服务-系统菜单控制器（动态路由/菜单管理）</b></p>
 *
 * @author yangcheng
 * @since 2026-11-19
 */
@RestResource(name = "系统菜单控制器", path = "/sys/menu")
class SysMenuController(
    private val sysMenuService: SysMenuService
) {

    /**
     * 菜单树（前端登录后拉取，渲染左侧菜单 + 前端据此动态注册路由）
     *
     * @return 顶级菜单（含嵌套 children）
     */
    @GetResource(name = "菜单树", path = "/list/tree")
    fun listTree(): List<SysMenu> = sysMenuService.listTree()

    /**
     * 新增菜单/路由
     *
     * @param menu 菜单 JSON（menuId 可空）
     * @return 新菜单 ID
     */
    @PostResource(name = "新增菜单/路由", path = "/add")
    fun add(@RequestBody menu: SysMenu): Long = sysMenuService.addMenu(menu)

    /**
     * 编辑菜单/路由
     *
     * @param menu 菜单 JSON（menuId 必填）
     */
    @PostResource(name = "编辑菜单/路由", path = "/edit")
    fun edit(@RequestBody menu: SysMenu) = sysMenuService.editMenu(menu)

    /**
     * 删除菜单/路由（软删除自身 + 子孙）
     *
     * @param menuId 菜单 ID
     */
    @PostResource(name = "删除菜单/路由（软删除自身 + 子孙）", path = "delete")
    fun delete(menuId: Long) = sysMenuService.deleteMenu(menuId)
}