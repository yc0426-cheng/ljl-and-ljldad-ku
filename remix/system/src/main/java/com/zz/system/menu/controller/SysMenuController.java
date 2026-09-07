package com.zz.system.menu.controller;

import com.zz.system.menu.entity.SysMenu;
import com.zz.system.menu.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p><b>系统服务-系统菜单控制器（动态路由/菜单管理）</b></p>
 *
 * <p>路径统一挂在 /system/menu 下，与网关路由 Path=/system/** 匹配，
 * 前端经 vite 代理 /api/system/menu/... → 网关 → system-server 调用。</p>
 *
 * <p>菜单/路由数据维护在 sys_menu 表，前端主页左侧菜单据此渲染；
 * 在「菜单管理」页里可增删改（这就是"管理加入路由"的数据入口）。</p>
 *
 * @author yangcheng
 * @since 2026-09-06
 */
@RestController
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 菜单树（前端登录后拉取，渲染左侧菜单 + 前端据此动态注册路由）
     *
     * @return 顶级菜单（含嵌套 children）
     */
    @GetMapping("/system/menu/list/tree")
    public List<SysMenu> listTree() {
        return sysMenuService.listTree();
    }

    /**
     * 新增菜单/路由
     *
     * @param menu 菜单 JSON（menuId 可空）
     * @return 新菜单 ID
     */
    @PostMapping("/system/menu/add")
    public Long add(@RequestBody SysMenu menu) {
        return sysMenuService.addMenu(menu);
    }

    /**
     * 编辑菜单/路由
     *
     * @param menu 菜单 JSON（menuId 必填）
     */
    @PostMapping("/system/menu/edit")
    public void edit(@RequestBody SysMenu menu) {
        sysMenuService.editMenu(menu);
    }

    /**
     * 删除菜单/路由（软删除自身 + 子孙）
     *
     * @param menuId 菜单 ID
     */
    @PostMapping("/system/menu/delete")
    public void delete(@RequestParam("menuId") Long menuId) {
        sysMenuService.deleteMenu(menuId);
    }
}
