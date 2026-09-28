package com.zz.system.role.enums;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p><b>系统服务-角色异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/28 18:12
 */
@Getter
public enum SysRoleExceptionEnum implements AbstractBaseExceptionEnum {
    ROLE_NAME_IS_ALREADY_EXISTS(1, "角色名称已存在"),
    IS_NOT_EDIT(2, "角色未进行修改")
    ;

    SysRoleExceptionEnum(Integer code, String message) {
        this.errorCode = code;
        this.errorMessage = message;
    }

    // 错误编码
    private final Integer errorCode;

    // 错误信息
    private final String errorMessage;
}
