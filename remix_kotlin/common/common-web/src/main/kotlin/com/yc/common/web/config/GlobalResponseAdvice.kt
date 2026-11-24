package com.yc.common.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.yc.common.core.anotation.RawResponse
import com.yc.common.core.constant.HttpHeaderConstant
import com.yc.common.core.pojo.ResultData
import org.springframework.core.MethodParameter
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/**
 * <p><b>通用核心工具-统一响应包装</b></p>
 * 自动将 Controller 返回值包装为 ResultData 结构 {code, message, data}
 *
 * 以下情况不包装，原样输出：
 * 1. 方法标注了 [RawResponse] 注解（如文件导出、二进制流）
 * 2. 返回类型为 byte[] / Resource / void（直接写响应体的接口）
 * 3. 返回值本身已是 ResultData（避免重复包装）
 * 4. 请求头 [HttpHeaderConstant.REQUEST_SOURCE] 为 feign（服务间调用，不包装）
 *
 * 特殊处理：当 Spring 选用 [StringHttpMessageConverter]（如返回 String 的接口），
 * 包装后的 ResultData 无法被该转换器写出，此时序列化为 JSON 字符串
 *
 * @param objectMapper JSON 序列化工具
 * @author yangcheng
 * @since 2026/11/21
 */
@RestControllerAdvice
class GlobalResponseAdvice(
    private val objectMapper: ObjectMapper
) : ResponseBodyAdvice<Any> {

    /**
     * 判断当前返回值是否需要包装
     *
     * @param returnType      方法返回类型（含方法元信息）
     * @param converterType   消息转换器类型
     * @return true 表示需要包装，false 表示原样输出
     */
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        // Feign 服务间调用不包装
        if (isFeignRequest()) {
            return false
        }
        // 标注了 @RawResponse 的接口原样输出
        if (returnType.hasMethodAnnotation(RawResponse::class.java)) {
            return false
        }
        // 文件流 / 二进制 / 直接写响应体的接口不包装
        val type = returnType.parameterType
        if (type == Byte::class.javaPrimitiveType ||
            type == Resource::class.java ||
            type == Void::class.javaPrimitiveType
        ) {
            return false
        }
        // 已经是 ResultData 类型的不重复包装
        return type != ResultData::class.java
    }

    /**
     * 在写出响应体前进行包装
     *
     * @param body                  原始返回值
     * @param returnType            方法返回类型
     * @param selectedContentType   响应内容类型
     * @param selectedConverterType 消息转换器类型
     * @param request               请求对象
     * @param response              响应对象
     * @return 包装后的响应体
     */
    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        // 空值包装为成功空数据
        val wrapped: Any = if (body == null) {
            ResultData.success<Any>(null)
        } else if (body is ResultData<*>) {
            // 已包装的 ResultData 直接放行
            body
        } else {
            ResultData.success(body)
        }

        // StringHttpMessageConverter 只能写 String，
        // 包装后的 ResultData 需序列化为 JSON 字符串
        if (selectedConverterType == StringHttpMessageConverter::class.java &&
            wrapped !is String
        ) {
            return objectMapper.writeValueAsString(wrapped)
        }
        return wrapped
    }

    /**
     * 判断当前请求是否来自 Feign 服务间调用
     * 通过请求头 [HttpHeaderConstant.REQUEST_SOURCE] 识别
     */
    private fun isFeignRequest(): Boolean {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes ?: return false
        return attrs.request.getHeader(HttpHeaderConstant.REQUEST_SOURCE) == HttpHeaderConstant.SOURCE_FEIGN
    }
}
