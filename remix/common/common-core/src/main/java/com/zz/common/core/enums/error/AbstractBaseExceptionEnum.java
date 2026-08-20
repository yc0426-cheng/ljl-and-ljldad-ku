package com.zz.common.core.enums.error;

/**
 * <p><b>通用工具-异常枚举通用接口</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 17:58
 */
public interface AbstractBaseExceptionEnum {

    /**
     * 获取模块编码（前补0到两位）
     * //todo 模块常量池
     */
    /**
     * 获取错误编码（前补0到三位）
     */
    Integer getErrorCode();

    /**
     * 获取错误信息
     */
    String getErrorMessage();

}
