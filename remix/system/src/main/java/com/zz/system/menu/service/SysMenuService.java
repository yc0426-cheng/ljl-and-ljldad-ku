package com.zz.system.menu.service;

import com.zz.system.menu.entity.SysMenu;

import java.util.List;

/**
 * 系统菜单业务接口
 *
 * @description 针对表【sys_menu(系统菜单表)】的数据库操作Service
 * @author yangcheng
 * @since 2026-09-06
 */
public interface SysMenuService {

    /**
     * 查询菜单树（未删除且启用状态，按 order_no/menu_id 升序，含 children 组装）
     *
     * @return 顶级菜单列表（内含嵌套 children）
     */
    List<SysMenu> listTree();

    /**
     * 新增菜单
     *
     * @param menu 菜单（menuId 可空，雪花自动生成；默认值见实现）
     * @return 新菜单 ID
     */
    Long addMenu(SysMenu menu);

    /**
     * 编辑菜单
     *
     * @param menu 菜单（menuId 必填）
     */
    void editMenu(SysMenu menu);

    /**
     * 删除菜单（软删除：自身 + 全部子孙）
     *
     * @param menuId 菜单 ID
     */
    void deleteMenu(Long menuId);
}
