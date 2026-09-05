package com.zz.api.system.user;

import com.zz.api.system.user.dto.SysUserFeignDTO;
import com.zz.common.core.annotation.TraceStep;
import com.zz.common.core.pojo.LoginUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p><b>系统服务-远端调用用户服务</b></p>
 *
 * <p>注意：方法上的 @TraceStep 表示"auth 侧视角的一次 Feign 远程调用步骤"，
 * 由 auth 的 OperationTraceAspect 处理，切面进入时会给落库端建一条 (feign) 步骤，
 * 并随 RPC 把 X-Trace-* 头带给 system，让 system 侧把自己的步骤续挂在这步下面。
 * 若该切面在 Feign 代理上未生效（代理顺序兼容性问题），可改用门面包装方式，见 TraceStep 注释。</p>
 *
 * @author yangcheng
 * @since 2026/9/1 16:46
 */
@FeignClient(name = "system-server", contextId = "SysUserFeignClient", path="/sys/user")
public interface SysUserFeignClient {

    /**
     * 根据账号获取用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    @TraceStep(module = "feign", callType = "feign")
    @GetMapping("/get/account")
    SysUserFeignDTO getUserInfoByAccount(@RequestParam("account") String account);

    /**
     * 登录密码错误
     *
     * @param userId 用户id
     */
    @TraceStep(module = "feign", callType = "feign")
    @PostMapping("/edit/error")
    void editError(@RequestParam("userId") Long userId);

    /**
     * 登录重置错误次数
     *
     * @param userId 用户id
     */
    @TraceStep(module = "feign", callType = "feign")
    @PostMapping("/edit/login")
    void editLogin(@RequestParam("userId") Long userId);

    /**
     * 创建用户登录信息
     *
     * @param userId 用户id
     * @return 用户登录信息
     */
    @TraceStep(module = "feign", callType = "feign")
    @GetMapping("/get/login/info")
    LoginUserInfo getLoginUserInfo(@RequestParam("userId") Long userId);
}
