package com.zz.system.user.converter;

import com.zz.system.user.entity.SysUserRole;
import com.zz.system.user.pojo.vo.SysUserRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * <p><b>系统服务-系统用户角色转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/10/2 16:44
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysUserRoleConverter {

    SysUserRoleVO entityToVO(SysUserRole sysUserRole);
}
