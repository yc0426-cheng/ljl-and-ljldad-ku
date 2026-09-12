package com.zz.system.user.mapper;

import com.zz.system.user.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zz.system.user.pojo.dto.SysUserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
* @author yangcheng
* @description 针对表【sys_user(用户信息表)】的数据库操作Mapper
* @createDate 2026-08-20 16:55:09
* @Entity com.zz.system.entity.SysUser
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    void edit(@Param("dto") SysUserDTO dto);
}




