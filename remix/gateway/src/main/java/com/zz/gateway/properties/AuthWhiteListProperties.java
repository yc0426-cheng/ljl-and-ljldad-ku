package com.zz.gateway.properties;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;

import java.util.List;

/**
 * <p><b>网关服务-白名单配置类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/1 12:01
 */
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthWhiteListProperties {

    /**
     * 白名单
     */
    @Getter
    @Setter
    private List<String> whiteList;
}

