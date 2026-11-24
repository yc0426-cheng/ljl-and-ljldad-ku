package com.yc.common.core.enums.error

/**
 * <p><b>通用工具-异常枚举通用接口</b></p>
 *
 * @author yangcheng
 * @since 2026/11/17 17:58
 */
interface AbstractBaseExceptionEnum {
    val errorCode: String
    val errorMessage: String
}