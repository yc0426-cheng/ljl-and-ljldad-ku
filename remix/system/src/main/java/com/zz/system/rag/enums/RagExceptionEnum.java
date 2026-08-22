package com.zz.system.rag.enums;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p><b>系统服务-RAG异常枚举</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Getter
public enum RagExceptionEnum implements AbstractBaseExceptionEnum {
    NOT_LOGIN(401, "未登录或登录已过期"),
    FILE_EMPTY_ERROR(400, "上传文件不能为空"),
    FILE_TYPE_ERROR(400, "仅支持 .md 格式的文档"),
    SERVICE_ERROR(500, "RAG 服务调用失败")
    ;

    RagExceptionEnum(Integer code, String message) {
        this.errorCode = code;
        this.errorMessage = message;
    }

    private final Integer errorCode;

    private final String errorMessage;
}
