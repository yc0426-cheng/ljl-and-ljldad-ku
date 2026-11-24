package com.yc.system.userRole.service

import com.baomidou.mybatisplus.extension.service.IService
import com.yc.system.userRole.entity.SysUserRole
import com.yc.system.userRole.pojo.vo.SysUserRoleVO

/**
 * 针对表【sys_user_role(用户角色绑定表)】的数据库操作Service
 *
 * @author yangcheng
 * @since 2026-10-24 20:02:28
 */
interface SysUserRoleService: IService<SysUserRole> {

    /**
     * 绑定角色
     *
     * @param userId     用户id
     * @param roleIdList 角色id列表
     */
    fun bindRole(userId: Long, roleIdList: List<Long>)

    /**
     * 查询列表
     *
     * @param userId 用户id
     * @return 用户绑定角色列表
     */
    fun page(userId: Long): List<SysUserRoleVO>
}