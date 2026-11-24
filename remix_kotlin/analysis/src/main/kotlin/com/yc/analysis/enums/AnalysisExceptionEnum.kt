package com.yc.analysis.enums

import com.yc.common.core.enums.error.AbstractBaseExceptionEnum

/**
 * <p><b>服务-模块</b></p>
 * 
 * @author yangcheng
 * @since 2026/11/20
 */
enum class AnalysisExceptionEnum(
    var code: Int,
    override val errorMessage: String
) : AbstractBaseExceptionEnum {
    OUT_OF_BIGGEST_SIZE(1, "文件大小超过最大限制"),
    CAN_NOT_READ_BINARY_FILE(2, "无法读取二进制文件"),
    INVALID_PATH(3, "非法路径"),
    FILE_NOT_FOUND(4, "文件不存在");

    override val errorCode: String = code.toString()
}