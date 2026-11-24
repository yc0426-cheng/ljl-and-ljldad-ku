package com.yc.system.role.converter

import com.yc.system.role.entity.SysRole
import com.yc.system.role.pojo.vo.SysRoleVO
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

/**
 * <p><b>系统服务-角色转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/11/19 18:28
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface SysRoleConverter {

    fun entityToVO(entity: SysRole): SysRoleVO
}