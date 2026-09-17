package com.zz.system.user.controller;

import com.zz.system.user.service.SysUserMiscService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>系统服务-用户杂项控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/16 11:32
 */
@Slf4j
@RestController("/sys/user")
@RequiredArgsConstructor
public class SysUserMiscController {

    private final SysUserMiscService sysUserMiscService;

    /**
     * 保存头像数据
     *
     * @param key ossKey
     */
    @PostMapping(name = "保存头像数据", path = "/save")
    public void save(@RequestParam("objectKey") String key){
        sysUserMiscService.insertOrUpdate(key);
    }

    /**
     * 获取头像数据
     *
     * @param userId 用户id
     * @return 头像地址
     */
    @GetMapping(name = "获取头像数据", path = "/get")
    public String getAvatar(@RequestParam("userId") Long userId){
        return sysUserMiscService.getAvatar(userId);
    }

    /**
     * 获取历史头像数据
     *
     * @param userId 用户id
     * @return 历史头像数据
     */
    @GetMapping(name = "获取历史头像数据", path = "/get/history")
    public String getHistoryAvatars(@RequestParam("userId") Long userId) {
        return sysUserMiscService.getHistoryAvatars(userId);
    }
}
