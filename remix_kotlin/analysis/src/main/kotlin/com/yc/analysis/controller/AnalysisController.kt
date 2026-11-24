package com.yc.analysis.controller

import com.yc.analysis.pojo.vo.FileContentVO
import com.yc.analysis.pojo.vo.FileTreeNode
import com.yc.analysis.pojo.vo.SearchResult
import com.yc.analysis.service.AnalysisService
import com.yc.common.core.anotation.GetResource
import com.yc.common.core.anotation.PostResource
import com.yc.common.core.anotation.RestResource


/**
 * <p><b>分析服务-分析控制器</b></p>
 * 
 * @author yangcheng
 * @since 2026/11/21
 */
@RestResource(name = "分析控制器", path = "/analysis")
class AnalysisController(
    private val analysisService: AnalysisService
) {

    @GetResource(name = "获取文件树形结构", path = "/get/tree")
    fun tree(): List<FileTreeNode> = analysisService.tree()

    @PostResource(name = "获取文件内容", path = "/get/content")
    fun getContent(path: String, startLine: Int, pageSize: Int): FileContentVO =
        analysisService.getContent(path, startLine, pageSize)

    @PostResource(name = "精确搜索", path = "/search/exactly")
    fun searchExactly(keyword: String): List<SearchResult> = analysisService.searchExactly(keyword)
}