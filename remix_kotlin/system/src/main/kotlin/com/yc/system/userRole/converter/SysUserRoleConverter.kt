package com.yc.system.userRole.converter

import com.yc.system.userRole.entity.SysUserRole
import com.yc.system.userRole.pojo.vo.SysUserRoleVO
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

/**
 * <p><b>系统服务-用户角色转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/11/19 00:14
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface SysUserRoleConverter {

    fun entityToVO(entity: SysUserRole): SysUserRoleVO
}