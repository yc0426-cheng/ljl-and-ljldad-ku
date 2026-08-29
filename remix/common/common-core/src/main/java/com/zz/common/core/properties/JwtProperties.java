package com.zz.common.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p><b>通用工具-jwt配置项</b></p>
 *
 * @author yangcheng
 * @since 2026/8/28 16:21
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * 密钥
     */
    private String secret;

    /**
     * 会话过期时间，单位：秒
     */
    private Integer sessionExpireTime;
}
