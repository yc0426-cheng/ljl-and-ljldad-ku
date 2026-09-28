package com.zz.book.enums;

import com.zz.common.core.enums.error.AbstractBaseExceptionEnum;
import lombok.Getter;

/**
 * <p><b>书籍服务-异常枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/18 12:41
 */
@Getter
public enum BookExceptionEnum implements AbstractBaseExceptionEnum {
    BOOK_NOT_EXIST(1, "书籍不存在"),
    BOOK_EXPORT_FAIL(2, "导出失败"),
    BOOK_NOT_AUTHORIZATION(3, "无权限修改此书籍"),
    BOOK_STATUS_EXCEPTION(4, "书籍状态异常"),
    BOOK_PAGE_ILLEGAL(5, "书籍页码不合法");

    BookExceptionEnum(Integer errorCode, String errorMessage) {
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
