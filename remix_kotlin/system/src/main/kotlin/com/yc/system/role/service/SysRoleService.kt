package com.yc.system.role.service

import com.baomidou.mybatisplus.extension.service.IService
import com.yc.system.role.entity.SysRole
import com.yc.system.role.pojo.dto.SysRoleDTO
import com.yc.system.role.pojo.param.SysRoleParam
import com.yc.system.role.pojo.vo.SysRoleVO
import jakarta.servlet.http.HttpServletResponse

/**
 * 针对表【sys_role】的数据库操作Service
 *
 * @author yangcheng
 * @since 2026-11-19 22:29:16
 */
interface SysRoleService : IService<SysRole> {

    /**
     * 查询角色
     *
     * @param param 查询参数
     * @return 角色列表
     */
    fun list(param: SysRoleParam): List<SysRoleVO>

    /**
     * 新建角色
     *
     * @param dto 新增角色数据
     */
    fun addRole(dto: SysRoleDTO)

    /**
     * 修改角色
     *
     * @param dto 修改角色数据
     */
    fun updateRole(dto: SysRoleDTO)

    /**
     * 删除角色
     *
     * @param roleId 角色id
     */
    fun deleteRole(roleId: Long)

    /**
     * 导出角色信息
     *
     * @param response 请求体
     * @param list 导出数据
     */
    fun exportRole(response: HttpServletResponse, list: List<SysRole>)
}