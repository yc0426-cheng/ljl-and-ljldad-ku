package com.zz.common.core.exception;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;

/**
 * <p><b>通用工具-业务级异常类</b></p>
 * 系统中所有的手动抛出的业务级异常
 *
 * @author yangcheng
 * @since 2026/8/20 18:00
 */
public final class BizException extends BaseException {
    /**
     * 使用异常枚举构造
     *
     * @param exceptionEnum 异常枚举
     */
    public BizException(AbstractBaseExceptionEnum exceptionEnum) {
        super(exceptionEnum.getErrorMessage());
        super.errorCode = String.format("%03d", exceptionEnum.getErrorCode());
        super.errorMessage = exceptionEnum.getErrorMessage();
    }

    /**
     * 使用自定义错误信息构造（错误码默认为 500）
     *
     * @param message 错误信息
     */
    public BizException(String message) {
        super(message);
        super.errorMessage = message;
    }
}
