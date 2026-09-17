package com.zz.common.core.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p><b>文件服务-oss配置类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/15 15:53
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OSSProperties {

    /**
     * 地域id
     */
    @NotBlank(message = "oss region 不能为空")
    private String region;

    /**
     * 接入点
     */
    @NotBlank(message = "oss endpoint 不能为空")
    private String endpoint;

    /**
     * 密钥id
     */
    @NotBlank(message = "oss accessKeyId 不能为空")
    private String accessKeyId;

    /**
     * 密钥
     */
    @NotBlank(message = "oss accessKeySecret 不能为空")
    private String accessKeySecret;

    /**
     * 桶名称
     */
    @NotBlank(message = "oss bucketName 不能为空")
    private String bucketName;

    /**
     * url前缀
     */
    private String urlPrefix;
}
