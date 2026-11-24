package com.yc.system.userRole.controller

import com.yc.common.core.anotation.PostResource
import com.yc.common.core.anotation.RestResource
import com.yc.system.userRole.pojo.vo.SysUserRoleVO
import com.yc.system.userRole.service.SysUserRoleService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

/**
 * <p><b>系统服务-用户角色控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/10/24 20:04
 */
@RestResource(name = "用户角色控制器", path = "/sys/user/role")
class SysUserRoleController(
    private val sysUserRoleService: SysUserRoleService
) {

    /**
     * 绑定角色
     *
     * @param userId     用户id
     * @param roleIdList 角色id列表
     */
    @PostResource(name = "绑定角色", path = "/bind")
    fun bindRole(
        @RequestParam userId: Long,
        @RequestBody roleIdList: List<Long>
    ) =
        sysUserRoleService.bindRole(userId, roleIdList)

    /**
     * 查询列表
     *
     * @param userId 用户id
     * @return 用户绑定角色列表
     */
    @PostResource(name = "查询列表", path = "/page")
    fun page(@RequestBody userId: Long): List<SysUserRoleVO> = sysUserRoleService.page(userId)
}