package com.zz.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p><b>模块-类说明</b></p>
 *
 * @author yangcheng
 * @since 2026/9/15 10:38
 */
public interface FileService {
    // ------------- 头像上传 ------------

    /**
     * 头像上传
     *
     * @param file   文件
     * @param userId 用户id
     */
    void uploadAvatar(MultipartFile file, Long userId);

    /**
     * 获取头像
     *
     * @param userId 用户id
     * @return url
     */
    String getAvatar(Long userId);

    /**
     * 获取历史头像
     *
     * @param userId 当前用户id
     * @return 头像oss地址列表
     */
    List<String> getHistoryAvatars(Long userId);

    // -------------- 书籍 --------------
    /**
     * 书籍上传
     *
     * @param file   文件
     * @param userId 用户id
     */
    void uploadBook(MultipartFile file, Long userId);

}
