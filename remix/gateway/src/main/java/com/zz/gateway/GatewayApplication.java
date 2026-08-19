package com.zz.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p><b>网关服务-启动类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/19 17:22
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.zz")
public class GatewayApplication {

    /**
     * 网关服务启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        log.info("GatewayApplication started successfully！");
    }
}
