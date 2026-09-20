package com.zz.common.log.annotation;

import java.lang.annotation.*;

/**
 * <p><b>核心工具类-日志记录注解</b></p>
 * <p>
 * &#064;description  使用此注解的方法，会将日志记录
 *
 * @author yangcheng
 * @since 2026/9/20 20:22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    // 操作描述
    String value() default "";

    LogModel model();

    enum LogModel {
        API,
        AUTH,
        BOOK,
        COMMON,
        FILE,
        GATEWAY,
        SYSTEM
    }
}
