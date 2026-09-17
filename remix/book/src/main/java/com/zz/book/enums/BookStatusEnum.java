package com.zz.book.enums;

import lombok.Getter;

/**
 * <p><b>书籍服务-状态枚举类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 16:41
 */
@Getter
public enum BookStatusEnum {

    ACTIVE(1, "有效"),
    INACTIVE(2, "无效");

    BookStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 编码
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;
}
