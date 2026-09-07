package com.zz.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.system.menu.entity.SysMenu;
import com.zz.system.menu.mapper.SysMenuMapper;
import com.zz.system.menu.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统菜单业务实现
 *
 * @description 针对表【sys_menu(系统菜单表)】的数据库操作Service实现
 * @author yangcheng
 * @since 2026-09-06
 */
@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    /** 菜单类型：目录 */
    private static final int TYPE_DIR = 1;
    /** 菜单类型：叶子菜单 */
    private static final int TYPE_MENU = 2;

    @Override
    public List<SysMenu> listTree() {
        List<SysMenu> all = baseMapper.selectList(new QueryWrapper<SysMenu>()
                .eq("del_flag", false)
                .eq("status", 1)
                .orderByAsc("order_no", "menu_id"));

        // 按 parentId 分组后逐层组装 children
        Map<Long, SysMenu> byId = new HashMap<>();
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : all) {
            byId.put(menu.getMenuId(), menu);
        }
        for (SysMenu menu : all) {
            Long pid = menu.getParentId();
            if (pid != null && byId.containsKey(pid)) {
                byId.get(pid).getChildren().add(menu);
            } else {
                roots.add(menu); // parent 为空或父节点已被删除/停用 → 当顶层处理
            }
        }
        return roots;
    }

    @Override
    public Long addMenu(SysMenu menu) {
        if (!StringUtils.hasText(menu.getMenuName())) {
            throw new IllegalArgumentException("菜单名称不能为空");
        }
        menu.setMenuType(menu.getMenuType() == null ? TYPE_MENU : menu.getMenuType());
        // 叶子菜单必须有路由地址；目录可不填
        if (menu.getMenuType() == TYPE_MENU && !StringUtils.hasText(menu.getRoutePath())) {
            throw new IllegalArgumentException("菜单（叶子）必须填写路由地址");
        }
        if (menu.getOrderNo() == null) {
            menu.setOrderNo(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(true);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        menu.setDelFlag(false);
        menu.setCreateTime(new Date());
        // menuId 由 MyBatis-Plus 雪花算法自动生成（id-type=assign_id），insert 后回填
        baseMapper.insert(menu);
        return menu.getMenuId();
    }

    @Override
    public void editMenu(SysMenu menu) {
        if (menu.getMenuId() == null) {
            throw new IllegalArgumentException("menuId 不能为空");
        }
        SysMenu exist = baseMapper.selectById(menu.getMenuId());
        if (exist == null || Boolean.TRUE.equals(exist.getDelFlag())) {
            throw new IllegalArgumentException("菜单不存在或已删除");
        }
        // 覆盖可编辑字段（创建时间/删除标记不可改）
        exist.setParentId(menu.getParentId());
        exist.setMenuName(menu.getMenuName());
        exist.setMenuType(menu.getMenuType());
        exist.setRoutePath(menu.getRoutePath());
        exist.setComponent(menu.getComponent());
        exist.setIcon(menu.getIcon());
        exist.setOrderNo(menu.getOrderNo() == null ? 0 : menu.getOrderNo());
        exist.setVisible(menu.getVisible() == null ? Boolean.TRUE : menu.getVisible());
        exist.setStatus(menu.getStatus() == null ? 1 : menu.getStatus());
        baseMapper.updateById(exist);
    }

    @Override
    public void deleteMenu(Long menuId) {
        if (menuId == null) {
            throw new IllegalArgumentException("menuId 不能为空");
        }
        // 收集自身 + 全部子孙 ID（递归），统一软删除
        List<Long> ids = new ArrayList<>();
        collectIds(menuId, ids);
        if (!ids.isEmpty()) {
            SysMenu tomb = new SysMenu();
            tomb.setDelFlag(true);
            baseMapper.update(tomb, new QueryWrapper<SysMenu>().in("menu_id", ids));
        }
        log.info("菜单删除 menuIds={}", ids);
    }

    /**
     * 递归收集某菜单及其全部子孙的 ID
     *
     * @param parentId 父菜单 ID
     * @param ids      收集结果
     */
    private void collectIds(Long parentId, List<Long> ids) {
        List<SysMenu> children = baseMapper.selectList(new QueryWrapper<SysMenu>()
                .eq("parent_id", parentId)
                .eq("del_flag", false));
        ids.add(parentId);
        for (SysMenu child : children) {
            collectIds(child.getMenuId(), ids);
        }
    }
}
