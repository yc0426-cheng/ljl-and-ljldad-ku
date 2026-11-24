package com.yc.system.role.controller

import com.yc.common.core.anotation.PostResource
import com.yc.common.core.anotation.RawResponse
import com.yc.common.core.anotation.RestResource
import com.yc.system.role.entity.SysRole
import com.yc.system.role.pojo.dto.SysRoleDTO
import com.yc.system.role.pojo.param.SysRoleParam
import com.yc.system.role.pojo.vo.SysRoleVO
import com.yc.system.role.service.SysRoleService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.RequestBody

/**
 * <p><b>系统服务-角色控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/28 19:27
 */
@RestResource(name = "角色控制器", path = "/sys/role")
class SysRoleController(
    private val sysRoleService: SysRoleService
) {

    /**
     * 查询角色
     *
     * @param param 查询参数
     * @return 角色列表
     */
    @PostResource(name = "查询角色", path = "/list")
    fun list(@RequestBody param: SysRoleParam): List<SysRoleVO> = sysRoleService.list(param)

    /**
     * 新建角色
     *
     * @param dto 新增角色数据
     */
    @PostResource(name = "新建角色", path = "/add")
    fun addRole(@RequestBody dto: SysRoleDTO) = sysRoleService.addRole(dto)

    /**
     * 修改角色
     *
     * @param dto 修改角色数据
     */
    @PostResource(name = "修改角色", path = "/update")
    fun updateRole(@RequestBody dto: SysRoleDTO) = sysRoleService.updateRole(dto)

    /**
     * 删除角色
     *
     * @param roleId 角色id
     */
    @PostResource(name = "删除角色", path = "/delete")
    fun deleteRole(@RequestBody roleId: Long) = sysRoleService.deleteRole(roleId)

    /**
     * 导出角色信息
     * 直接写文件流到响应体，标注 @RawResponse 跳过统一响应包装
     *
     * @param response 请求体
     * @param list     导出数据
     */
    @RawResponse
    @PostResource(name = "导出角色信息", path = "/export")
    fun exportRole(
        response: HttpServletResponse,
        @RequestBody list: List<SysRole>
    ) = sysRoleService.exportRole(response, list)
}