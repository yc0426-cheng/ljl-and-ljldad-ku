package com.yc.analysis.pojo.vo

/**
 * <p><b>分析服务-文件内容返回结果类</b></p>
 * 
 * @author yangcheng
 * @since 2026/11/20
 */
data class FileContentVO(
    val lines: List<String>,
    val startLine: Int,
    val hasMore: Boolean
)
