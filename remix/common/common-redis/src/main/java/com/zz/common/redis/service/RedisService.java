package com.zz.common.redis.service;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p><b>通用工具-redis集成工具类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/21 21:00
 */
@Data
@Service
@RequiredArgsConstructor
public class RedisService {

    /**
     * redis模板
     */
    @Getter
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * JSON对象映射器
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * 删除key
     *
     * @param key key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /* ********************************** String ********************************** */

    /**
     * 设置值，时间单位默认秒
     *
     * @param key     redis键
     * @param value   值
     * @param timeout 过期时间
     * @param <T>     类型
     */
    public <T> void set(String key, T value, long timeout) {
        set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置值
     *
     * @param key     redis键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @param <T>     类型
     */
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        String json = safeWriteValueAsString(value);
        redisTemplate.opsForValue().set(key, json, timeout, unit);
    }

    /**
     * 获取数据并转换类型
     *
     * @param key   redis key
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return T
     */
    public <T> T get(String key, Class<T> clazz) {
        String value = redisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 安全转换类型
     *
     * @param t 数据
     * @return json字符串
     */
    private <T> String safeWriteValueAsString(T t) {
        try {
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
