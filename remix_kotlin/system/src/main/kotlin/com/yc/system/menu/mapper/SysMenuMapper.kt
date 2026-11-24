package com.yc.system.menu.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.yc.system.menu.entity.SysMenu
import org.apache.ibatis.annotations.Mapper

/**
 * 系统菜单表 Mapper
 *
 * @description 针对表【sys_menu(系统菜单表)】的数据库操作Mapper
 * @author yangcheng
 * @since 2026-11-19
 */
@Mapper
interface SysMenuMapper : BaseMapper<SysMenu>