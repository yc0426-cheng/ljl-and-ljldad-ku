package com.zz.file.controller;

import com.zz.common.log.annotation.OperationLog;
import com.zz.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.List;

/**
 * <p><b>文件服务-控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/15 10:37
 */
@Slf4j
@RestController("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // ----------- 用户头像 ------------

    /**
     * 头像上传
     *
     * @param file   文件
     * @param userId 用户id
     */
    @OperationLog(value = "头像上传", model = OperationLog.LogModel.FILE)
    @PostMapping(name = "头像上传", path = "/avatar/upload")
    public void uploadingAvatar(@RequestParam("file") MultipartFile file,
                                @RequestParam("userId") Long userId) {
        fileService.uploadAvatar(file, userId);
    }

    /**
     * 获取头像
     *
     * @param userId 用户id
     * @return url
     */
    @OperationLog(value = "获取头像", model = OperationLog.LogModel.FILE)
    @GetMapping(name = "获取头像", path = "/get/avatar")
    public String getAvatar(@RequestParam("userId") Long userId) {
        return fileService.getAvatar(userId);
    }

    /**
     * 获取历史头像
     *
     * @param userId 当前用户id
     * @return 头像oss地址列表
     */
    @OperationLog(value = "获取历史头像", model = OperationLog.LogModel.FILE)
    @GetMapping(name = "获取历史头像", path = "/get/history/avatars")
    public List<String> getHistoryAvatars(@RequestParam("userId") Long userId) {
        return fileService.getHistoryAvatars(userId);
    }

    // ------------- 书籍上传 -------------

    /**
     * 书籍上传
     *
     * @param file   文件
     * @param userId 用户id
     */
    @OperationLog(value = "书籍上传", model = OperationLog.LogModel.FILE)
    @PostMapping(name = "书籍上传", path = "/book/upload")
    public void uploadBook(MultipartFile file, Long userId) {
        fileService.uploadBook(file, userId);
    }

    /**
     * 从 OSS 下载文件流并写入目标 OutputStream
     * 使用 alibabacloud-oss-v2 的 GetObjectResult.body()
     *
     * @param contentUrl oss地址
     * @param target     输出流
     */
    @OperationLog(value = "从oss下载并写入", model = OperationLog.LogModel.FILE)
    @PostMapping(name = "从oss下载并写入", path = "/download/oss")
    void downloadOssToStream(String contentUrl, OutputStream target) throws Exception {
        fileService.downloadOssToStream(contentUrl, target);
    }
}
