package com.yc.system.user.converter

import com.yc.api.system.dto.SysUserFeignDTO
import com.yc.system.user.entity.SysUser
import com.yc.system.user.pojo.dto.SysUserDTO
import com.yc.system.user.pojo.vo.SysUserVO
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

/**
 * <p><b>系统服务-系统用户转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/11/16 20:49
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface SysUserConverter {

    /**
     * 实体类转vo
     */
    fun entityToVO(sysUser: SysUser): SysUserVO

    /**
     * dto转实体类
     */
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "passErrorCount", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    fun dto2Entity(dto: SysUserDTO): SysUser

    /**
     * 实体类转远程调用类
     */
    fun entityToFeignDTO(sysUser: SysUser): SysUserFeignDTO
}