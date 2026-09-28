package com.zz.system.role.convert;

import com.zz.system.role.entity.SysRole;
import com.zz.system.role.pojo.vo.SysRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * <p><b>系统服务-角色转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/28 18:28
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysRoleConverter {

    SysRoleVO entityToVO(SysRole entity);
}
