package com.zz.common.core.exception;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;

/**
 * <p><b>通用核心工具-基础异常抽象类</b></p>
 * 此系统下的所有异常类都是此类的子类
 *
 * @author yangcheng
 * @since 2026/8/20 17:56
 */
public sealed class BaseException extends RuntimeException
        permits BizException {

    /**
     * 使用错误信息构造
     *
     * @param errorMessage 错误信息
     */
    protected BaseException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * 异常枚举引用，用于获取 HTTP 状态码
     * //todo 获取http码的依赖
     */
    protected AbstractBaseExceptionEnum exceptionEnum;

    /**
     * 错误编码
     */
    protected String errorCode;

    /**
     * 错误信息
     */
    protected String errorMessage;

    /**
     * 获取错误编码
     *
     * @return 错误编码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

}
