package com.yc.common.core.pojo

/**
 * <p><b>通用核心工具-统一响应结构</b></p>
 * 所有接口统一返回 {code, message, data} 结构，由 GlobalResponseAdvice 自动包装
 *
 * @param code    状态码，200 成功 / 500 失败
 * @param message 提示信息
 * @param data    业务数据
 * @author yangcheng
 * @since 2026/11/21
 */
data class ResultData<T>(
    val code: Int,
    val message: String,
    val data: T?
) {

    companion object {
        /** 成功状态码 */
        const val SUCCESS_CODE = 200

        /** 失败状态码 */
        const val ERROR_CODE = 500

        /**
         * 构建成功响应
         *
         * @param data 业务数据，可为空
         */
        fun <T> success(data: T? = null): ResultData<T> = ResultData(SUCCESS_CODE, "success", data)

        /**
         * 构建成功响应（自定义提示语）
         *
         * @param message 提示信息
         * @param data    业务数据，可为空
         */
        fun <T> success(message: String, data: T? = null): ResultData<T> = ResultData(SUCCESS_CODE, message, data)

        /**
         * 构建失败响应（默认 500）
         *
         * @param message 错误提示
         */
        fun <T> error(message: String): ResultData<T> = ResultData(ERROR_CODE, message, null)

        /**
         * 构建失败响应（自定义状态码）
         *
         * @param code    状态码
         * @param message 错误提示
         */
        fun <T> error(code: Int, message: String): ResultData<T> = ResultData(code, message, null)
    }
}
