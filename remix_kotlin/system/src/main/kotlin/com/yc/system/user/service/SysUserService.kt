package com.yc.system.user.service

import com.baomidou.mybatisplus.extension.service.IService
import com.yc.api.system.dto.SysUserFeignDTO
import com.yc.common.core.pojo.LoginUserInfo
import com.yc.system.user.entity.SysUser
import com.yc.system.user.pojo.dto.SysUserDTO
import com.yc.system.user.pojo.param.SysUserParam
import com.yc.system.user.pojo.vo.SysUserVO

/**
 * @author yangcheng
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service
 * @createDate 2026-11-16 20:08:09
 */
interface SysUserService : IService<SysUser> {

    /**
     * 根据账号获取用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    fun getUserInfoByAccount(account: String): SysUserFeignDTO

    /**
     * 登录密码错误
     *
     * @param userId 用户id
     */
    fun editError(userId: Long)

    /**
     * 登录重置错误次数
     *
     * @param userId 用户id
     */
    fun editLogin(userId: Long)

    /**
     * 创建用户登录信息
     *
     * @param userId 用户id
     * @return 用户登录信息
     */
    fun getLoginUserInfo(userId: Long): LoginUserInfo

    /**
     * 更新最后登录时间（登出时由 auth 经 feign 调用）
     *
     * @param userId 用户id
     */
    fun setLastLoginTime(userId: Long)

    /**
     * 分页用户信息
     *
     * @return 用户列表
     */
    fun page(): List<SysUserVO>

    /**
     * 获取用户信息列表
     *
     * @return 用户列表
     */
    fun getList(param: SysUserParam): List<SysUserVO>

    /**
     * 新增用户
     *
     * @param dto 新增用户数据
     */
    fun add(dto: SysUserDTO)

    /**
     * 修改用户
     *
     * @param dto 修改用户信息
     */
    fun edit(dto: SysUserDTO)
}
