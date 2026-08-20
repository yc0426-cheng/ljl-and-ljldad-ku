package com.zz.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p><b>系统服务-启动类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 11:08
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.zz")
public class SystemApplication {

    /**
     * 系统服务启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
        log.info("SystemApplication started successfully! ");
    }
}
