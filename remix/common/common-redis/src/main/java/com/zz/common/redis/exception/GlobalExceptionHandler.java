package com.zz.common.redis.exception;

import com.zz.common.core.exception.BaseException;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p><b>通用工具-全局异常处理</b></p>
 *
 * <p>统一把异常转成 {@code {code, message, data}} 结构返回，
 * 避免给前端返回默认错误页。</p>
 *
 * @author yangcheng
 * @since 2026/8/22
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（BizException）
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getErrorCode(), e.getMessage());
        return Result.error(toCode(e.getErrorCode()), e.getMessage());
    }

    /**
     * 其他基础异常
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBaseException(BaseException e) {
        log.warn("基础异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 参数校验失败（@Valid / @NotBlank 等）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return Result.error(400, msg);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙: " + e.getMessage());
    }

    /**
     * 错误码字符串（如 "001"）转 int，解析失败用 500
     */
    private int toCode(String errorCode) {
        if (errorCode == null) {
            return 500;
        }
        try {
            return Integer.parseInt(errorCode);
        } catch (NumberFormatException e) {
            return 500;
        }
    }
}
