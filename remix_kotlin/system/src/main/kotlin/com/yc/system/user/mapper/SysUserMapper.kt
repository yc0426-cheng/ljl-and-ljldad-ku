package com.yc.system.user.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.yc.system.user.entity.SysUser
import com.yc.system.user.pojo.dto.SysUserDTO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 * @author yangcheng
 * @description 针对表【sys_user(用户信息表)】的数据库操作Mapper
 * @createDate 2026-11-17 11:08:09
 * @Entity com.yc.system.entity.SysUser
 */
@Mapper
interface SysUserMapper : BaseMapper<SysUser> {

    fun edit(@Param("dto") dto: SysUserDTO)
}