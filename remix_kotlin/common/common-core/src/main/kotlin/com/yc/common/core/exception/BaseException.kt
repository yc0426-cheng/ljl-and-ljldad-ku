package com.yc.common.core.exception

import com.yc.common.core.enums.error.AbstractBaseExceptionEnum

/**
 * <p><b>通用核心工具-基础异常抽象类</b></p>
 * 此系统下的所有异常类都是此类的子类
 *
 * @author yangcheng
 * @since 2026/11/16 17:56
 */
sealed class BaseException(
    protected val exceptionEnum: AbstractBaseExceptionEnum,
    protected val errorCode: String,
    protected val errorMessage: String
) : RuntimeException(errorMessage) {

    val code: String get() = errorCode
    val msg: String get() = errorMessage

    /**
     * <p><b>通用工具-业务级异常类</b></p>
     * 系统中所有的手动抛出的业务级异常
     *
     * @author yangcheng
     * @since 2026/11/16 18:00
     */
    final class BizException(
        exceptionEnum: AbstractBaseExceptionEnum,
        errorCode: String,
        errorMessage: String
    ) : BaseException(exceptionEnum, errorCode, errorMessage) {
        // 加入构造器，自动加入errorCode以及errorMessage
        constructor(exceptionEnum: AbstractBaseExceptionEnum) : this(
            exceptionEnum,
            exceptionEnum.errorCode,
            exceptionEnum.errorMessage
        )
    }

    /**
     * <p><b>通用核心工具-系统级异常</b></p>
     *
     * @author yangcheng
     * @since 2026/11/17 12:30
     */
    open class SysException(
        exceptionEnum: AbstractBaseExceptionEnum,
        errorCode: String,
        errorMessage: String
    ) : BaseException(exceptionEnum, errorCode, errorMessage) {
        class SysDangerException(
            exceptionEnum: AbstractBaseExceptionEnum
        ) : SysException(
            exceptionEnum,
            exceptionEnum.errorCode,
            exceptionEnum.errorMessage
        ) {
            companion object {
                fun unauthorizedAccess() = SysDangerException(DangerExceptionEnum.UNAUTHORIZED_ACCESS)
            }

            /**
             * <p><b>通用核心工具-系统级危险异常</b></p>
             *
             * @author yangcheng
             * @since 2026/11/17 12:33
             */
            private enum class DangerExceptionEnum(
                private val code: Int,
                override val errorMessage: String
            ) : AbstractBaseExceptionEnum {
                UNAUTHORIZED_ACCESS(0, "越权访问"),
                ACCOUNT_LOCK(1, "账号锁定"),
                BLACK_IP_ACCESS(2, "黑名单ip访问");

                override val errorCode: String
                    get() = String.format("%03d", code)
            }
        }
    }
}