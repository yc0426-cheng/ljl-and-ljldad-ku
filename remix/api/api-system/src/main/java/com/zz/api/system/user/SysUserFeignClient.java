package com.zz.api.system.user;

import com.zz.api.system.user.dto.SysUserFeignDTO;
import com.zz.common.core.pojo.LoginUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p><b>系统服务-远端调用用户服务</b></p>
 *
 * @author yangcheng
 * @since 2026/9/1 16:46
 * todo 新增nacos，删除硬编码url
 */
@FeignClient(name = "system-user", url = "http://localhost:12000")
public interface SysUserFeignClient {

    /**
     * 根据账号获取用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    @GetMapping("/sys/user/get/account")
    SysUserFeignDTO getUserInfoByAccount(@RequestParam("account") String account);

    /**
     * 登录密码错误
     *
     * @param userId 用户id
     */
    @PostMapping("/sys/user/edit/error")
    void editError(@RequestParam("userId") Long userId);

    /**
     * 登录重置错误次数
     *
     * @param userId 用户id
     */
    @PostMapping("/sys/user/edit/login")
    void editLogin(@RequestParam("userId") Long userId);

    /**
     * 创建用户登录信息
     *
     * @param userId 用户id
     * @return 用户登录信息
     */
    @GetMapping("/sys/user/get/login/info")
    LoginUserInfo getLoginUserInfo(@RequestParam("userId") Long userId);
}
