package com.yc.system.user.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.yc.api.system.dto.SysUserFeignDTO
import com.yc.common.core.context.LoginUserHolder
import com.yc.common.core.exception.BaseException
import com.yc.common.core.pojo.LoginUserInfo
import com.yc.commonRedis.service.RedisService
import com.yc.system.user.converter.SysUserConverter
import com.yc.system.user.entity.SysUser
import com.yc.system.user.enums.SysUserExceptionEnum
import com.yc.system.user.mapper.SysUserMapper
import com.yc.system.user.pojo.dto.SysUserDTO
import com.yc.system.user.pojo.param.SysUserParam
import com.yc.system.user.pojo.vo.SysUserVO
import com.yc.system.user.service.SysUserService
import org.springframework.stereotype.Service
import java.util.*

/**
 * @author yangcheng
 * @description 针对表【sys_user(用户信息表)】的数据库操作Service实现
 * @createDate 2026-11-17 11:35:09
 */
@Service
class SysUserServiceImpl(
    val redisService: RedisService,
    val sysUserMapper: SysUserMapper,
    val sysUserConverter: SysUserConverter
) : ServiceImpl<SysUserMapper, SysUser>(), SysUserService {

    override fun getUserInfoByAccount(account: String): SysUserFeignDTO {
        val user = getOne(QueryWrapper<SysUser>().eq("account", account))
        return sysUserConverter.entityToFeignDTO(user)
    }

    override fun editError(userId: Long) {
        val user = getById(userId) ?: throw BaseException.BizException(SysUserExceptionEnum.SYS_USER_NOT_NULL)
        if (user.passErrorCount + 1 > 3) {
            redisService.set("login_locked", true, 5)
            throw BaseException.BizException(SysUserExceptionEnum.OUT_OF_ERROR_COUNT)
        }

        user.passErrorCount++

        super<ServiceImpl>.updateById(user)
    }

    override fun editLogin(userId: Long) {
        val user = getById(userId) ?: throw BaseException.BizException(SysUserExceptionEnum.SYS_USER_NOT_NULL)
        user.passErrorCount = 0
        user.lastLoginTime = Date()
        super<ServiceImpl>.updateById(user)
    }

    override fun getLoginUserInfo(userId: Long): LoginUserInfo {
        val user = getById(userId) ?: throw BaseException.BizException(SysUserExceptionEnum.SYS_USER_NOT_NULL)
        return LoginUserInfo(
            userId, user.account,
            user.name, user.phone,
            user.email
        )
    }

    override fun setLastLoginTime(userId: Long) {
        val user = getById(userId) ?: throw BaseException.BizException(SysUserExceptionEnum.SYS_USER_NOT_NULL)
        user.lastLoginTime = Date()
        super<ServiceImpl>.updateById(user)
    }

    override fun page(): List<SysUserVO> {
        return list().map { sysUserConverter.entityToVO(it) }
    }

    override fun getList(param: SysUserParam): List<SysUserVO> {
        val qw = QueryWrapper<SysUser>()
            .like(param.account.isNotBlank(), "account", param.account)
            .like(param.name.isNotBlank(), "name", param.name)

        return list(qw).map { sysUserConverter.entityToVO(it) }
    }

    override fun add(dto: SysUserDTO) {
        val userInfo = LoginUserHolder.get()
        userInfo ?: return

        super<ServiceImpl>.save(sysUserConverter.dto2Entity(dto))
    }

    override fun edit(dto: SysUserDTO) {
        sysUserMapper.edit(dto)
    }
}