package com.yc.auth.service

import cn.hutool.core.date.DateUtil
import cn.hutool.jwt.JWT
import com.yc.api.system.SysUserFeignClient
import com.yc.api.system.enums.SysUserStatusEnum
import com.yc.auth.enums.LoginExceptionEnum
import com.yc.auth.pojo.LoginDTO
import com.yc.common.core.constant.RedisKeyConstant
import com.yc.common.core.exception.BaseException
import com.yc.common.core.pojo.LoginUserInfo
import com.yc.common.core.properties.JwtProperties
import com.yc.commonRedis.service.RedisService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

/**
 * <p><b>认证服务-service服务类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/17 19:45
 */
@Service
class LoginService(
    private val sysUserFeignClient: SysUserFeignClient,
    private val redisService: RedisService,
    private val jwtProperties: JwtProperties
) {

    private val log = LoggerFactory.getLogger(LoginService::class.java)

    /**
     * 登录
     *
     * @param loginDTO 登录的账户和密码
     * @return token
     */
    fun login(loginDTO: LoginDTO): String {
        val account = loginDTO.account
        val password = loginDTO.password

        val user = sysUserFeignClient.getUserInfoByAccount(account)

        if (user.status != SysUserStatusEnum.ENABLE) {
            throw BaseException.BizException(LoginExceptionEnum.USER_LOCKED)
        }

        val encoder = BCryptPasswordEncoder()

        if (!encoder.matches(password, user.password)) {
            sysUserFeignClient.editError(user.userId)
            throw BaseException.BizException(LoginExceptionEnum.PASSWORD_ERROR)
        }

        sysUserFeignClient.editLogin(user.userId)

        val token = JWT.create()
            .setPayload("userId", user.userId)
            .setIssuedAt(Date())
            .setExpiresAt(DateUtil.offsetHour(Date(), 30))
            .setKey(jwtProperties.secret.toByteArray())
            .sign()

        val userInfo = sysUserFeignClient.getLoginUserInfo(user.userId)

        userInfo.token = token

        redisService.set(RedisKeyConstant.TOKEN + token, userInfo, 120 * 60)

        return token
    }

    /**
     * 查询账号当前的登录密码错误次数
     *
     * @param account 账号
     * @return 密码错误次数；账号不存在时返回 0
     */
    fun getErrorCount(account: String): Int {
        return sysUserFeignClient.getUserInfoByAccount(account).passErrorCount
    }

    /**
     * 校验token是否有效
     * 有效标准：redis中存在 TOKEN:&lt;token&gt; 键（未过期），且不在登出黑名单中
     *
     * @param token 纯token（不含Bearer前缀，由controller剥离）
     * @return token对应的用户信息
     */
    fun checkToken(token: String): LoginUserInfo {
        if (token.isBlank()) throw BaseException.BizException(LoginExceptionEnum.TOKEN_INVALID)

        val inBlackList = redisService.redisTemplate
            .hasKey(RedisKeyConstant.BLACK_LIST_PREFIX + token)

        if (inBlackList) throw BaseException.BizException(LoginExceptionEnum.TOKEN_INVALID)

        return redisService
            .get(RedisKeyConstant.TOKEN + token, LoginUserInfo::class.java)
            ?: throw BaseException.BizException(LoginExceptionEnum.TOKEN_INVALID)
    }

    fun logout(token: String) {
        if (token.isBlank()) return

        val userInfo = redisService.get(RedisKeyConstant.TOKEN + token, LoginUserInfo::class.java) ?: return

        val ttl = redisService.redisTemplate.getExpire(token)

        redisService.delete(RedisKeyConstant.TOKEN + token)

        redisService.set(RedisKeyConstant.BLACK_LIST_PREFIX + token, userInfo, ttl + 1)

        try {
            userInfo.userId.let { sysUserFeignClient.setLastLoginTime(System.currentTimeMillis()) }
        } catch (e: Exception) {
            log.error("登出-更新最后登录时间失败 userId={}", userInfo.userId, e)
        }
    }
}