package com.zz.gateway.enums;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p><b>网关服务-过滤器异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/1 11:24
 */
@Getter
public enum FilterExceptionEnum implements AbstractBaseExceptionEnum {
    TOKEN_NOT_EXISTS(1, "token不存在"),
    USER_NOT_EXISTS(2, "用户信息不存在"),
    ;

    FilterExceptionEnum(Integer code, String message){
        this.errorCode = code;
        this.errorMessage = message;
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
