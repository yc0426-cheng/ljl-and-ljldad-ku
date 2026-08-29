package com.zz.auth.controller;

import cn.hutool.core.util.StrUtil;
import com.zz.auth.pojo.LoginDTO;
import com.zz.auth.service.LoginService;
import com.zz.common.core.pojo.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>认证中心-登录相关控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 10:55
 */
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 用户登录接口
     *
     * @param loginDTO 接收的登录账号和密码
     * @return token
     */
    @PostMapping(name = "用户登录",path = "/auth/login")
    public String login(@RequestBody LoginDTO loginDTO){
        return loginService.login(loginDTO);
    }

    /**
     * 校验token是否有效
     * 前端 request.ts 请求拦截器自动携带 Authorization: Bearer &lt;token&gt;，
     * 这里从请求头取出并剥离 Bearer 前缀后交给 service 校验
     *
     * @param authorization 请求头 Authorization（可空，空则由 service 判无效）
     * @return token对应的用户信息；token无效时抛 BizException(TOKEN_INVALID) → 500
     */
    @PostMapping(name = "校验token", path = "/auth/check")
    public LoginUserInfo check(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // 剥离 "Bearer " 前缀，得到纯token
        String token = (StrUtil.isNotBlank(authorization) && authorization.startsWith("Bearer "))
                ? authorization.substring(7)
                : authorization;
        return loginService.checkToken(token);
    }

    /**
     * 登出
     */
    @PostMapping(name = "登出", path = "/auth/logout")
    public void logout(){
        loginService.logout();
    }

    // 修改密码
}
