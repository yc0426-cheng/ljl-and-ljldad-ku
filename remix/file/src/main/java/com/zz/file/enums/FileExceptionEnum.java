package com.zz.file.enums;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p><b>文件服务-异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/16 10:59
 */
@Getter
public enum FileExceptionEnum implements AbstractBaseExceptionEnum {
    FILE_NOT_EMPTY(1, ""),
    FORMAT_DOES_NOT_MATCH(2, "格式不正确，请使用jpg|jpeg|png|gif这四种格式"),
    UPLOAD_FAIL(3, "上传失败");


    FileExceptionEnum(Integer code, String message) {
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
