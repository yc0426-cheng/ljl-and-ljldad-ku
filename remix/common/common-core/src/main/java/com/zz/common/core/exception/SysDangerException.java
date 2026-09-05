package com.zz.common.core.exception;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * <p><b>模块-类说明</b></p>
 *
 * @author yangcheng
 * @since 2026/9/5 12:33
 */
public final class SysDangerException extends SysException {

    /**
     * 使用危险异常枚举构造
     *
     * @param exceptionEnum 异常枚举
     */
    public SysDangerException(AbstractBaseExceptionEnum exceptionEnum) {
        super(exceptionEnum.getErrorMessage());
        super.errorCode = String.format("%03d", exceptionEnum.getErrorCode());
        super.errorMessage = exceptionEnum.getErrorMessage();
    }

    /**
     * 越权访问错误
     */
    public static SysDangerException unauthorizedAccess() {
        return new SysDangerException(DangerExceptionEnum.UNAUTHORIZED_ACCESS);
    }

    /**
     * 危险异常的
     */
    @Getter
    private enum DangerExceptionEnum implements AbstractBaseExceptionEnum {
        /**
         * 未授权访问
         */
        UNAUTHORIZED_ACCESS(0, "越权访问"),
        /**
         * 账号锁定
         */
        ACCOUNT_LOCK(1, "账号锁定"),
        /**
         * 黑名单IP访问
         */
        BLACK_IP_ACCESS(2, "黑名单ip访问"),
        ;

        DangerExceptionEnum(Integer errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        /**
         * 错误编码
         */
        private final Integer errorCode;

        /**
         * 错误信息
         */
        private final String errorMessage;
    }
}
