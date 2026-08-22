package com.zz.auth.controller;

import com.zz.auth.pojo.LoginDTO;
import com.zz.auth.service.LoginService;
import com.zz.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String login(LoginDTO loginDTO){
        return loginService.login(loginDTO);
    }

    // 校验token过期时间

    /**
     * 用户登出接口（需携带登录令牌）
     *
     * @return 统一返回结果
     */
    @PostMapping(name = "用户登出", path = "/auth/logout")
    public Result<Void> logout(){
        loginService.logout();
        return Result.success();
    }

    // 修改密码
}
