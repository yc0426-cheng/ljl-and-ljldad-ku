package com.zz.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <p><b>认证服务-启动类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/19 18:13
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.zz")
@EnableFeignClients(basePackages = "com.zz")
public class AuthApplication {

    /**
     * 认证服务启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        log.info("AuthApplication started successfully！");
    }
}
