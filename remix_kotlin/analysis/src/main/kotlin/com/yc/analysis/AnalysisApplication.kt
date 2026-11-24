package com.yc.analysis

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.yc"])
class AnalysisApplication {
    companion object {
        val log = LoggerFactory.getLogger(AnalysisApplication::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<AnalysisApplication>(*args)
    AnalysisApplication.log.info("AnalysisApplication started successfully!")
}