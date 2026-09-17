package com.zz.book;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <p><b>书籍服务-启动类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 14:52
 */
@Slf4j
@SpringBootApplication(scanBasePackages ="com.zz")
@EnableFeignClients(basePackages = "com.zz")
public class BookApplication {

    /**
     * 书籍服务启动入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BookApplication.class, args);
        log.info("BookApplication started successfully! ");
    }
}
