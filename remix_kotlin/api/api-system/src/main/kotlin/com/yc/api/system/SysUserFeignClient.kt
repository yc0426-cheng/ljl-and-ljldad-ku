package com.yc.api.system

import com.yc.api.system.dto.SysUserFeignDTO
import com.yc.common.core.pojo.LoginUserInfo
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "system-server", contextId = "SysUserFeignClient", path = "/sys/user")
interface SysUserFeignClient {

    /**
     * 根据账号获取用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    @GetMapping("/get/account")
    fun getUserInfoByAccount(@RequestParam("account") account: String): SysUserFeignDTO

    /**
     * 登录密码错误
     *
     * @param userId 用户id
     */
    @PostMapping("/edit/error")
    fun editError(@RequestParam("userId") userId: Long)

    /**
     * 登录重置错误次数
     *
     * @param userId 用户id
     */
    @PostMapping("/edit/login")
    fun editLogin(@RequestParam("userId") userId: Long)

    /**
     * 更新最后登录时间（登出时回写 sys_user.last_login_time）
     *
     * @param userId 用户id
     */
    @PostMapping("/edit/last-login-time")
    fun setLastLoginTime(@RequestParam("userId") userId: Long)

    /**
     * 创建用户登录信息
     *
     * @param userId 用户id
     * @return 用户登录信息
     */
    @GetMapping("/get/login/info")
    fun getLoginUserInfo(@RequestParam("userId") userId: Long): LoginUserInfo
}
