package com.zz.system.user.controller;

import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.system.user.entity.SysUser;
import com.zz.system.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>系统服务-系统用户控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/1 16:58
 */
@RestController
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 根据账号获取用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    @GetMapping(path = "/sys/user/get/account")
    public SysUser getUserInfoByAccount(String account) {
        return sysUserService.getUserInfoByAccount(account);
    }

    /**
     * 登录密码错误
     *
     * @param userId 用户id
     */
    @PostMapping("/sys/user/edit/error")
    public void editError(Long userId){
        sysUserService.editError(userId);
    }

    /**
     * 登录重置错误次数
     *
     * @param userId 用户id
     */
    @PostMapping("/sys/user/edit/login")
    public void editLogin(Long userId){
        sysUserService.editLogin(userId);
    }

    /**
     * 创建用户登录信息
     *
     * @param userId 用户id
     * @return 用户登录信息
     */
    @GetMapping("/sys/user/get/login/info")
    public LoginUserInfo getLoginUserInfo(Long userId){
        return sysUserService.getLoginUserInfo(userId);
    }
}
