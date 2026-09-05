package com.zz.common.core.exception;

/**
 * <p><b>通用核心工具-系统级异常</b></p>
 *
 * @author yangcheng
 * @since 2026/9/5 12:30
 */
public sealed class SysException extends BaseException
        permits SysDangerException {

    /**
     * 使用错误信息构造
     *
     * @param errorMessage 错误信息
     */
    protected SysException(String errorMessage) {
        super(errorMessage);
    }
}
