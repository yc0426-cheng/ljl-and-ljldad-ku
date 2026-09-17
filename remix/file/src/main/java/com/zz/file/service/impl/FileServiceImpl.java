package com.zz.file.service.impl;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.PresignOptions;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.GetObjectRequest;
import com.aliyun.sdk.service.oss2.models.PresignResult;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.zz.api.system.user.SysUserMiscClient;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.properties.OSSProperties;
import com.zz.file.enums.FileExceptionEnum;
import com.zz.file.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * <p><b>系统服务-用户杂项服务实现类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/15 10:51
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private OSSClient ossClient;

    private final OSSProperties ossProperties;

    private final SysUserMiscClient sysUserMiscClient;

    // 初始化
    @PostConstruct
    public void init() {
        // V2 客户端必须指定 region，凭证从环境变量读取
        this.ossClient = OSSClient.newBuilder()
                .region(ossProperties.getRegion())
                .credentialsProvider(
                        new StaticCredentialsProvider(
                                ossProperties.getAccessKeyId(),
                                ossProperties.getAccessKeySecret()
                        )
                )
                .build();
    }

    // 关闭时机
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            try {
                ossClient.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void uploadAvatar(MultipartFile file, Long userId) {
        // 判断 file 有值
        if (file == null || file.isEmpty()) {
            throw new BizException(FileExceptionEnum.FILE_NOT_EMPTY);
        }
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 判断格式
        if (!suffix.matches("\\.(jpg|jpeg|png|gif)$")) {
            throw new BizException(FileExceptionEnum.FORMAT_DOES_NOT_MATCH);
        }

        // 生成 objectKey
        String objectKey = "avatar/"
                + userId + "_"
                + UUID.randomUUID().toString().replace("-", "")
                + suffix;

        // 上传oss
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.newBuilder()
                    .bucket(ossProperties.getBucketName())
                    .key(objectKey)
                    .body(BinaryData.fromStream(in))
                    .build();

            ossClient.putObject(request);
        } catch (IOException e) {
            throw new BizException(FileExceptionEnum.UPLOAD_FAIL);
        }

        //存入数据库
        sysUserMiscClient.save(objectKey);
    }

    @Override
    public String getAvatar(Long userId) {
        // 从数据库内获取到
        String avatar = sysUserMiscClient.getAvatar(userId);

        // 请求 oss
        GetObjectRequest request = GetObjectRequest.newBuilder()
                .bucket(ossProperties.getBucketName())
                .key(avatar)
                .build();

        // 设置url过期时间
        PresignOptions options = PresignOptions.newBuilder()
                .expiration(Duration.ofMinutes(180))
                .build();

        // 生成预签名 url
        PresignResult result = ossClient.presign(request, options);
        return result.url().replace("+", "%2B");
    }

    @Override
    public List<String> getHistoryAvatars(Long userId) {
        // 远程调用获取头像数据
        String historyAvatar = sysUserMiscClient.getHistoryAvatars(userId);
        // 获取每一个头像数据
        String[] historyAvatars = historyAvatar.split("[,，]");

        if (historyAvatars.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(historyAvatars);
    }

    @Override
    public void uploadBook(MultipartFile file, Long userId) {

    }
}
