package com.yc.system.userRole.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.yc.system.userRole.entity.SysUserRole
import org.apache.ibatis.annotations.Mapper

/**
 * 针对表【sys_user_role(用户角色绑定表)】的数据库操作Mapper
 *
 * @author yangcheng
 * @since 2026-11-19 20:02:28
 */
@Mapper
interface SysUserRoleMapper : BaseMapper<SysUserRole>