package com.zz.system.user.converter;

import com.zz.api.system.user.dto.SysUserFeignDTO;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.pojo.dto.SysUserDTO;
import com.zz.system.user.pojo.vo.SysUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * <p><b>系统服务-系统用户转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/10 16:32
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysUserConverter {

    /**
     * 实体类转vo
     */
    SysUserVO entityToVO(SysUser sysUser);

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
    SysUser dto2Entity(SysUserDTO dto);

    /**
     * 实体类转远程调用类
     */
    SysUserFeignDTO  entityToFeignDTO(SysUser sysUser);
}
