package com.zz.system.user.enums;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p><b>系统服务-用户异常枚举</b></p>
 *
 * @author yangcheng
 * @since 2026/8/21 11:51
 */
@Getter
public enum SysUserExceptionEnum implements AbstractBaseExceptionEnum {
    OUT_OF_ERROR_COUNT(1, "密码错误三次，请等待5分钟")
    ;

    SysUserExceptionEnum(Integer code, String message) {
        this.errorCode = code;
        this.errorMessage = message;
    }

    private final Integer errorCode;

    /**/
    private final String errorMessage;
}
