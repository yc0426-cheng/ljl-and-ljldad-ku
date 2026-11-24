package com.yc.commonRedis.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    val redisTemplate: RedisTemplate<String, String>,
    val objectMapper: ObjectMapper
) {

    /**
     * 删除key
     *
     * @param key key
     */
    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    /* ********************************** String ********************************** */

    /**
     * 设置值，时间单位默认秒
     *
     * @param key     redis键
     * @param value   值
     * @param timeout 过期时间
     */
    fun <T> set(key: String, value: T, timeout: Long) {
        set(key, value, timeout, TimeUnit.SECONDS)
    }

    /**
     * 设置值
     *
     * @param key     redis键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    fun <T> set(key: String, value: T, timeout: Long, unit: TimeUnit) {
        val json = safeWriteValueAsString(value)
        redisTemplate.opsForValue().set(key, json, timeout, unit)
    }

    /**
     * 获取数据并转换类型
     *
     * @param key   redis key
     * @param clazz 目标类型
     * @return T
     */
    fun <T> get(key: String, clazz: Class<T>): T? {
        val value = redisTemplate.opsForValue().get(key)
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            objectMapper.readValue(value, clazz)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 安全转换类型
     *
     * @param t 数据
     * @return json字符串
     */
    private fun <T> safeWriteValueAsString(t: T): String {
        return try {
            objectMapper.writeValueAsString(t)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }
}
