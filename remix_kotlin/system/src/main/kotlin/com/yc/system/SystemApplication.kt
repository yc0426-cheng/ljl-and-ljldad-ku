package com.yc.system

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

/**
 * <p><b>系统服务-启动类</b></p>
 *
 * @author yangcheng
 * @since 2026/11/17 11:08
 */
@SpringBootApplication(scanBasePackages = ["com.yc"])
@EnableFeignClients(basePackages = ["com.yc"])
class SystemApplication{
    companion object {
        val log: Logger? = LoggerFactory.getLogger(SystemApplication::class.java)
    }
}


/**
 * 系统服务启动入口
 *
 * @param args 命令行参数
 */
fun main(args: Array<String>) {
    runApplication<SystemApplication>(*args)
    SystemApplication.log?.info("SystemApplication started successfully!")
}
