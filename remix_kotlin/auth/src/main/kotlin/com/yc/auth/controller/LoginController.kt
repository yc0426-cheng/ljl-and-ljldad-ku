package com.yc.auth.controller

import com.yc.auth.pojo.LoginDTO
import com.yc.auth.service.LoginService
import com.yc.common.core.anotation.PostResource
import com.yc.common.core.anotation.RestResource
import com.yc.common.core.pojo.LoginUserInfo
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

/**
 * <p><b>认证中心-登录相关控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 10:55
 */
@RestResource(name = "登录相关控制器", path = "/auth")
class LoginController(
    private val loginService: LoginService,
) {

    /**
     * 用户登录
     *
     * @param loginDTO 接收的登录账号和密码
     * @return token
     */
    @PostResource(name = "用户登录", path = "/login")
    fun login(@RequestBody loginDTO: LoginDTO): String = loginService.login(loginDTO)

    /**
     * 根据账号查询登录密码错误次数
     *
     * @param account 账号
     * @return 密码错误次数；账号不存在时返回 0
     */
    @PostResource(name = "根据账号查询登录密码错误次数", path = "/error-count")
    fun getErrorCount(@RequestParam("account") account: String): Int = loginService.getErrorCount(account)

    /**
     * 校验token是否有效
     * 前端 request.ts 请求拦截器自动携带 Authorization: Bearer &lt;token&gt;，
     * 这里从请求头取出并剥离 Bearer 前缀后交给 service 校验
     *
     * @param authorization 请求头 Authorization（可空，空则由 service 判无效）
     * @return token对应的用户信息；token无效时抛 BizException(TOKEN_INVALID) → 500
     */
    @PostResource(name = "校验token是否有效", path = "/check")
    fun check(@RequestHeader(value = "Authorization", required = false) authorization: String): LoginUserInfo {
        val token = if (authorization.isNotBlank() && authorization.startsWith("Bearer "))
            authorization.substring(7)
        else authorization

        return loginService.checkToken(token)
    }

    /**
     * 登出
     * <p>与 check 同理：从请求头 Authorization 剥离 Bearer 后拿纯 token 交给 service。
     * 不能依赖 LoginUserHolder（ThreadLocal）——本项目没有任何过滤器往里面写值，登出时必为 null。</p>
     *
     * @param authorization 请求头 Authorization（可空，空则由 service 幂等返回）
     */
    @PostResource(name = "登出", path = "/logout")
    fun logout(@RequestHeader(value = "Authorization", required = false) authorization: String) {
        val token = if (authorization.isNotBlank() && authorization.startsWith("Bearer "))
            authorization.substring(7)
        else authorization

        loginService.logout(token)
    }
}