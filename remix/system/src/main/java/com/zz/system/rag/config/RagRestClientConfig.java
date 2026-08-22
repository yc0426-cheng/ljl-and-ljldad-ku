package com.zz.system.rag.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * <p><b>系统服务-RKBase HTTP 客户端配置</b></p>
 *
 * <p>组装访问 RKBase（Python RAG 服务）的 RestClient，
 * 默认带上 X-API-Key 请求头；api-key 为空时跳过（RKBase 侧同样跳过鉴权）。</p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Configuration
public class RagRestClientConfig {

    @Value("${rkbase.base-url:http://127.0.0.1:8000}")
    private String baseUrl;

    @Value("${rkbase.api-key:}")
    private String apiKey;

    @Bean
    public RestClient ragRestClient(RestClient.Builder builder) {
        RestClient.Builder restClientBuilder = builder.baseUrl(baseUrl);
        if (StrUtil.isNotBlank(apiKey)) {
            restClientBuilder.defaultHeader("X-API-Key", apiKey);
        }
        return restClientBuilder.build();
    }
}
