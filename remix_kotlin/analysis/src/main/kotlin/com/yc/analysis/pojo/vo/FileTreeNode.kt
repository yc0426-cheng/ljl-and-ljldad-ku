package com.yc.analysis.pojo.vo


/**
 * <p><b>分析服务-文件树节点类</b></p>
 * 
 * @author yangcheng
 * @since 2026/11/20
 */
data class FileTreeNode(
    val name: String,
    var path: String,
    val isDir: Boolean,
    val size: Long = 0,
    val children: MutableList<FileTreeNode> = mutableListOf()
)
