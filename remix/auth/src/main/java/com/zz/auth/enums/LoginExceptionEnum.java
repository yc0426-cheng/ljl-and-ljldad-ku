package com.zz.auth.enums;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p><b>认证服务-登录异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/8/20 17:40
 */
@Getter
public enum LoginExceptionEnum implements AbstractBaseExceptionEnum {
    NOT_ENABLE(1, "账号未被启用"),
    PASSWORD_ERROR(2, "密码错误")
    ;

    LoginExceptionEnum(Integer code, String message){
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
