package com.yc.common.web.config

import com.yc.common.core.exception.BaseException
import com.yc.common.core.pojo.ResultData
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * <p><b>通用核心工具-全局异常处理</b></p>
 * 捕获 Controller 抛出的异常，统一转换为 ResultData 错误结构返回前端，
 * 避免异常堆栈直接暴露给客户端
 *
 * 处理优先级：
 * 1. [BaseException] 业务异常 —— 返回业务错误码与提示语
 * 2. [Exception] 其他异常 —— 返回通用系统错误提示
 *
 * @author yangcheng
 * @since 2026/11/21
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    /**
     * 处理业务异常
     * 业务代码主动抛出的 [BaseException]，携带明确的错误码与提示语
     *
     * @param e 业务异常
     * @return 携带业务错误信息的统一响应
     */
    @ExceptionHandler(BaseException::class)
    fun handleBizException(e: BaseException): ResultData<Nothing> {
        log.error("业务异常 | 错误码:{} | 错误信息:{}", e.code, e.msg)
        return ResultData.error(ResultData.ERROR_CODE, e.msg)
    }

    /**
     * 处理其他未捕获异常
     * 兜底处理，避免系统异常堆栈泄露给前端
     *
     * @param e 异常
     * @return 通用系统错误响应
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResultData<Nothing> {
        log.error("系统异常", e)
        return ResultData.error(ResultData.ERROR_CODE, "系统异常，请稍后重试")
    }
}
