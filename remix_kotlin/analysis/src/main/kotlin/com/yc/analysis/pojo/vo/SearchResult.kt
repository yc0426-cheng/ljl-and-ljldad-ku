package com.yc.analysis.pojo.vo

/**
 * <p><b>分析服务-搜索结果类</b></p>
 * 
 * @author yangcheng
 * @since 2026/11/20
 */
data class SearchResult(
    val path: String,
    val lineNo: Int,
    val content: String
)
