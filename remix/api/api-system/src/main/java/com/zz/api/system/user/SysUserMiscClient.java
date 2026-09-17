package com.zz.api.system.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p><b>远程调用-用户杂项接口</b></p>
 *
 * @author yangcheng
 * @since 2026/9/16 11:24
 */
@FeignClient(name = "system-server", contextId = "SysUserMiscClient", path = "/sys/avatar")
public interface SysUserMiscClient {

    /**
     * 存入头像数据
     *
     * @param objectKey oss key
     */
    @PostMapping("/save")
    void save(@RequestParam("objectKey") String objectKey);

    /**
     * 获取头像数据
     *
     * @param userId 用户id
     * @return 获取头像数据
     */
    @GetMapping("/get")
    String getAvatar(@RequestParam("userId") Long userId);

    /**
     * 获取历史头像数据
     *
     * @param userId 用户id
     * @return 历史头像数据
     */
    @GetMapping("/get/history")
    String getHistoryAvatars(@RequestParam("userId") Long userId);
}
