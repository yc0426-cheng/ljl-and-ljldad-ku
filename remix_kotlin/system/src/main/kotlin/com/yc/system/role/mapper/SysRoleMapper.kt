package com.yc.system.role.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.yc.system.role.entity.SysRole
import org.apache.ibatis.annotations.Mapper

/**
 * 针对表【sys_role】的数据库操作Mapper
 *
 * @author yangcheng
 * @since 2026-11-19 22:29:16
 */
@Mapper
interface SysRoleMapper : BaseMapper<SysRole>