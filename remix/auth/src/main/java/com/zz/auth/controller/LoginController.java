package com.zz.auth.controller;

import com.zz.auth.pojo.LoginDTO;
import com.zz.auth.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    // 校验token过期时间

    /**
     * 登出
     */
    @PostMapping(name = "登出", path = "/auth/logout")
    public void logout(){
        loginService.logout();
    }

    // 修改密码
}
