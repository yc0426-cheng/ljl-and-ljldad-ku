package com.yc.auth

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

/**
 * <p><b>认证服务-启动类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/17 11:08
 */
@SpringBootApplication(scanBasePackages = ["com.yc"])
@EnableFeignClients(basePackages = ["com.yc"])
class AuthApplication {
    companion object {
        val log: Logger? = LoggerFactory.getLogger(AuthApplication::class.java)
    }
}

/**
 * 系统服务启动入口
 *
 * @param args 命令行参数
 */
fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
    AuthApplication.log?.info("AuthApplication started successfully!")
}