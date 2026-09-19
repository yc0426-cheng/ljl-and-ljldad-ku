package com.zz.api.file.avatar;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * <p><b>远程调用-书籍相关功能</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 15:58
 */
@FeignClient(name = "file-server", contextId = "BookFeignClient", path = "/file")
public interface FileFeignClient {

    /**
     * 书籍上传
     *
     * @param file   文件
     * @param userId 用户id
     */
    @PostMapping(name = "书籍上传", path = "/book/upload")
    void uploadBook(MultipartFile file, Long userId);

    /**
     * 从 OSS 下载文件流并写入目标 OutputStream
     * 使用 alibabacloud-oss-v2 的 GetObjectResult.body()
     */
    @PostMapping("/download/oss")
    void downloadOssToStream(String contentUrl, OutputStream target);
}
