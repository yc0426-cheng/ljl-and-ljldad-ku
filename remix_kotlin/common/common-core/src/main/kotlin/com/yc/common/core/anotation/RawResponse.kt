package com.yc.common.core.anotation

/**
 * <p><b>通用核心工具-原样响应注解</b></p>
 * 标注在 Controller 方法上，表示该接口返回原始内容（如文件流、二进制数据），
 * 跳过统一响应包装（ResultData），直接输出原始响应体
 *
 * @author yangcheng
 * @since 2026/11/21
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RawResponse
