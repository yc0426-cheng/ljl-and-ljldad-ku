package com.yc.system.user.controller

import com.yc.api.system.dto.SysUserFeignDTO
import com.yc.common.core.anotation.GetResource
import com.yc.common.core.anotation.PostResource
import com.yc.common.core.anotation.RestResource
import com.yc.common.core.pojo.LoginUserInfo
import com.yc.system.user.pojo.dto.SysUserDTO
import com.yc.system.user.pojo.param.SysUserParam
import com.yc.system.user.pojo.vo.SysUserVO
import com.yc.system.user.service.SysUserService
import org.springframework.web.bind.annotation.RequestParam

/**
 * <p><b>系统服务-系统用户控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/11/17 14:58
 */
@RestResource(name = "系统用户控制器", path = "/sys/user")
class SysUserController(
    private val sysUserService: SysUserService
) {
    /**
     * 根据账号获取用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    @GetResource(name = "根据账号获取用户信息", path = "/get/account")
    fun getUserInfoByAccount(@RequestParam account: String): SysUserFeignDTO =
        sysUserService.getUserInfoByAccount(account)

    /**
     * 登录密码错误
     *
     * @param userId 用户id
     */
    @PostResource(name = "登录密码错误", "/edit/error")
    fun editError(userId: Long) = sysUserService.editError(userId)

    /**
     * 登录重置错误次数
     *
     * @param userId 用户id
     */
    @PostResource(name = "登录重置错误次数", "/edit/login")
    fun editLogin(userId: Long) = sysUserService.editLogin(userId)

    /**
     * 创建用户登录信息
     *
     * @param userId 用户id
     * @return 用户登录信息
     */
    @GetResource(name = "创建用户登录信息", "/get/login/info")
    fun getLoginUserInfo(userId: Long): LoginUserInfo = sysUserService.getLoginUserInfo(userId)

    /**
     * 更新最后登录时间（登出时由 auth 经 feign 调用）
     *
     * @param userId 用户id
     */
    @PostResource(name = "更新最后登录时间", "/edit/last-login-time")
    fun setLastLoginTime(userId: Long) = sysUserService.setLastLoginTime(userId)

    /**
     * 分页用户信息
     *
     * @return 用户列表
     */
    @PostResource(name = "分页用户信息", "/page")
    fun page(): List<SysUserVO> = sysUserService.page()

    /**
     * 获取用户信息列表
     *
     * @return 用户列表
     */
    @GetResource(name = "获取用户信息列表", "/list")
    fun list(param: SysUserParam): List<SysUserVO> = sysUserService.getList(param)

    /**
     * 新增用户
     *
     * @param dto 新增用户数据
     */
    @PostResource(name = "新增用户", "/add")
    fun add(dto: SysUserDTO) = sysUserService.add(dto)

    /**
     * 修改用户
     *
     * @param dto 修改用户信息
     */
    @PostResource(name = "修改用户", "/edit")
    fun edit(dto: SysUserDTO) = sysUserService.edit(dto)

}