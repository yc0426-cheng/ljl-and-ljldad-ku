package com.yc.analysis.service

import com.yc.analysis.pojo.vo.FileContentVO
import com.yc.analysis.pojo.vo.FileTreeNode
import com.yc.analysis.pojo.vo.SearchResult

/**
 * <p><b>分析服务-分析业务类</b></p>
 * 
 * @author yangcheng
 * @since 2026/11/20
 */
interface AnalysisService {

    /**
     * 搜索树
     *
     * @return 文件树结构
     */
    fun tree(): List<FileTreeNode>

    /**
     * 获取文本信息
     *
     * @return 信息
     */
    fun getContent(path: String, startLine: Int, pageSize: Int): FileContentVO

    /**
     * 精确匹配搜索
     *
     * @param keyword 关键字
     * @return 搜索结果
     */
    fun searchExactly(keyword: String): List<SearchResult>
}