package com.yc.system.menu.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.yc.common.core.exception.BaseException
import com.yc.system.menu.entity.SysMenu
import com.yc.system.menu.enums.SysMenuExceptionEnum
import com.yc.system.menu.mapper.SysMenuMapper
import com.yc.system.menu.service.SysMenuService
import org.springframework.stereotype.Service
import java.util.*

/**
 * 系统菜单业务实现
 *
 * @description 针对表【sys_menu(系统菜单表)】的数据库操作Service实现
 * @author yangcheng
 * @since 2026-11-19
 */
@Service
class SysMenuServiceImpl : ServiceImpl<SysMenuMapper, SysMenu>(), SysMenuService {

    companion object {
        const val TYPE_DIR = 1
        const val TYPE_MENU = 2
    }

    override fun listTree(): List<SysMenu> {
        val qw = QueryWrapper<SysMenu>().eq("del_flag", false)
            .eq("status", 1)
            .orderByAsc("order_no", "menu_id")

        val allList = super<ServiceImpl>.list(qw)

        val byId = HashMap<Long, SysMenu>()
        val roots = ArrayList<SysMenu>()

        for (menu in allList) {
            byId[menu.menuId] = menu
        }

        for (menu in allList) {
            val pid = menu.parentId

            if (pid != null && byId.containsKey(pid)) byId[pid]?.children?.add(menu) else roots.add(menu)
        }

        return roots
    }

    override fun addMenu(menu: SysMenu): Long {
        if (menu.menuName.isBlank()) throw BaseException.BizException(SysMenuExceptionEnum.ROLE_NAME_IS_NOT_EMPTY)

        menu.menuType ?: TYPE_MENU

        if (menu.menuType == TYPE_MENU && menu.routePath.isBlank()) throw BaseException.BizException(
            SysMenuExceptionEnum.ROUTE_IS_EMPTY
        )

        menu.delFlag = false
        menu.createTime = Date()

        super<ServiceImpl>.save(menu)
        return menu.menuId
    }

    override fun editMenu(menu: SysMenu) {
        val exist = super<ServiceImpl>.getById(menu.menuId)
        if (exist == null || exist.delFlag) {
            throw BaseException.BizException(SysMenuExceptionEnum.MENU_IS_NOT_EXIST_OR_DELETED)
        }

        exist.parentId = menu.parentId
        exist.menuName = menu.menuName
        exist.menuType = menu.menuType
        exist.routePath = menu.routePath
        exist.component = menu.component
        exist.icon = menu.icon
        exist.orderNo = menu.orderNo
        exist.visible = menu.visible
        exist.status = menu.status

        super.saveOrUpdate(exist)
    }

    override fun deleteMenu(menuId: Long) {
        val ids = ArrayList<Long>()
        collectIds(menuId, ids)

        if (ids.isNotEmpty()) {
            super<ServiceImpl>.removeBatchByIds(ids)
        }
    }

    /**
     * 递归收集某菜单及其全部子孙的 ID
     *
     * @param parentId 父菜单 ID
     * @param ids      收集结果
     */
    private fun collectIds(parentId: Long, ids: MutableList<Long>) {
        val qw = QueryWrapper<SysMenu>()
            .eq("parent_id", parentId)
            .eq("del_flag", false)

        val children = super<ServiceImpl>.list(qw)

        ids.add(parentId)
        for (menu in children) {
            collectIds(menu.menuId, ids)
        }
    }
}