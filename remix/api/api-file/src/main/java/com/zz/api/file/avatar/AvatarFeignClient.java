package com.zz.api.file.avatar;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p><b>远程调用-头像上传功能</b></p>
 *
 * @author yangcheng
 * @since 2026/9/15 17:35
 */
@FeignClient(name = "file-server", contextId = "AvatarFeignClient", path = "/file")
public interface AvatarFeignClient {

    // ------------ 头像相关 ----------
    /**
     * 上传头像
     *
     * @param file 头像文件
     * @param userId 用户id
     */
    @PostMapping(name = "头像上传", path = "/avatar/upload")
    void upload(@RequestParam("file") MultipartFile file,  @RequestParam("userId") Long userId);

    /**
     * 获取头像
     *
     * @param userId 用户id
     * @return url
     */
    @GetMapping(name = "获取头像", path = "/get/avatar")
    String getAvatar(@RequestParam("userId") Long userId);

    /**
     * 获取历史头像
     *
     * @param userId 当前用户id
     * @return 头像oss地址列表
     */
    @GetMapping(name = "获取历史头像", path = "/get/history/avatars")
    List<String> getHistoryAvatars(@RequestParam("userId") Long userId);
}
