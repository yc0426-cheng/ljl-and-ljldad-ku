package com.yc.system.userRole.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.yc.system.userRole.converter.SysUserRoleConverter
import com.yc.system.userRole.entity.SysUserRole
import com.yc.system.userRole.mapper.SysUserRoleMapper
import com.yc.system.userRole.pojo.vo.SysUserRoleVO
import com.yc.system.userRole.service.SysUserRoleService
import org.springframework.stereotype.Service

/**
 * 针对表【sys_user_role(用户角色绑定表)】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-11-19 20:02:28
 */
@Service
class SysUserRoleServiceImpl(
    private val sysUserRoleConverter: SysUserRoleConverter,
) : ServiceImpl<SysUserRoleMapper, SysUserRole>(), SysUserRoleService {

    override fun bindRole(userId: Long, roleIdList: List<Long>) {
        val qw = QueryWrapper<SysUserRole>()
            .eq("user_id", userId)
        super<ServiceImpl>.remove(qw)

        val sysUserRoleList = ArrayList<SysUserRole>()
        for (roleId in roleIdList) {
            val sysUserRole = SysUserRole().apply {
                userRoleId = 0
                this.userId = userId
                this.roleId = roleId
            }

            sysUserRoleList.add(sysUserRole)
        }

        super<ServiceImpl>.saveBatch(
            sysUserRoleList.stream().distinct().toList()
        )
    }

    override fun page(userId: Long): List<SysUserRoleVO> {
        val qw = QueryWrapper<SysUserRole>()
            .eq("user_id", userId)
        val list = super<ServiceImpl>.list(qw)
        return list.stream()
            .map { sysUserRoleConverter.entityToVO(it) }
            .toList()
    }
}