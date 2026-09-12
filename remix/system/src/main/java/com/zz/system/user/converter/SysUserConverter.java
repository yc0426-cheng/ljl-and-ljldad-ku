package com.zz.system.user.converter;

import com.zz.system.user.entity.SysUser;
import com.zz.system.user.pojo.dto.SysUserDTO;
import com.zz.system.user.pojo.vo.SysUserVO;
import org.mapstruct.Mapper;
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
    SysUser dto2Entity(SysUserDTO dto);
}
