package com.yc.analysis.service.impl

import com.yc.analysis.enums.AnalysisExceptionEnum
import com.yc.analysis.pojo.vo.FileContentVO
import com.yc.analysis.pojo.vo.FileTreeNode
import com.yc.analysis.pojo.vo.SearchResult
import com.yc.analysis.properties.LogProperties
import com.yc.analysis.service.AnalysisService
import com.yc.common.core.exception.BaseException
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.mutableListOf
import kotlin.io.path.fileSize
import kotlin.io.path.forEachLine
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString
import kotlin.io.path.reader

/**
 * <p><b>分析服务-分析业务接口实现类</b></p>
 * 
 * @author yangcheng
 * @since 2026/11/20
 */
@Service
class AnalysisServiceImpl(
    private val logProperties: LogProperties
) : AnalysisService {

    override fun tree(): List<FileTreeNode> {
        val root = Path.of(logProperties.path)
        if (!Files.exists(root)) return emptyList()

        val list = Files.list(root).use { stream ->
            stream.sorted { a, b ->
                val aDir = Files.isDirectory(a)
                val bDir = Files.isDirectory(b)

                when {
                    aDir && !bDir -> -1
                    !aDir && bDir -> 1
                    else -> a.fileName.toString().compareTo(b.fileName.toString())
                }
            }
                .map { buildNode(it) }
                .toList()
        }

        return list
    }

    override fun getContent(path: String, startLine: Int, pageSize: Int): FileContentVO {
        val file = resolve(path)
        if (!Files.isRegularFile(file)) {
            throw BaseException.BizException(AnalysisExceptionEnum.FILE_NOT_FOUND)
        }
        if (file.fileSize() > 500L * 1024 * 1024) {
            throw BaseException.BizException(AnalysisExceptionEnum.OUT_OF_BIGGEST_SIZE)
        }

        // 读前 1KB 探测二进制
        val header = file.inputStream().use { it.readNBytes(1024) }
        if (header.contains(0.toByte())) {
            throw BaseException.BizException(AnalysisExceptionEnum.CAN_NOT_READ_BINARY_FILE)
        }

        val lines = mutableListOf<String>()
        var lineNo = 0
        var hasMore = false
        BufferedReader(file.reader()).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                lineNo++
                if (lineNo < startLine) continue
                if (lines.size >= pageSize) {
                    hasMore = true
                    break
                }
                lines.add(line)
            }
        }

        return FileContentVO(lines, startLine, hasMore)
    }

    override fun searchExactly(keyword: String): List<SearchResult> {
        if (keyword.isBlank()) return emptyList()

        val root = Path.of(logProperties.path)
        if (!Files.exists(root)) return emptyList()

        val results = mutableListOf<SearchResult>()
        Files.walk(root).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .filter { it.fileSize() <= 500L * 1024 * 1024 }
                .forEach { file ->
                    // 跳过二进制文件
                    val header = runCatching {
                        file.inputStream().use { it.readNBytes(1024) }
                    }.getOrNull() ?: return@forEach
                    if (header.contains(0.toByte())) return@forEach

                    val relativePath = root.relativize(file).pathString
                    var lineNo = 0
                    runCatching {
                        file.forEachLine { line ->
                            lineNo++
                            if (line.contains(keyword)) {
                                results.add(SearchResult(relativePath, lineNo, line))
                            }
                        }
                    }
                }
        }

        return results
    }

    /**
     * 将前端传入的相对路径解析为安全的绝对路径，防止路径穿越
     *
     * @param path 前端传入的相对路径
     * @return 根目录内的绝对路径
     */
    private fun resolve(path: String): Path {
        val root = Path.of(logProperties.path).normalize()
        val resolved = root.resolve(path).normalize()
        if (!resolved.startsWith(root)) {
            throw BaseException.BizException(AnalysisExceptionEnum.INVALID_PATH)
        }
        return resolved
    }

    /**
     * 递归调用遍历node节点，并对文件及文件夹排序
     *
     * @param path 路径
     * @return 树结构
     */
    private fun buildNode(path: Path): FileTreeNode {
        val isDir = path.isDirectory()
        val children = if (isDir) {
            Files.list(path).use { stream ->
                stream.sorted { a, b ->
                    val aDir = Files.isDirectory(a)
                    val bDir = Files.isDirectory(b)

                    when {
                        aDir && !bDir -> -1
                        !aDir && bDir -> 1
                        else -> a.fileName.toString().compareTo(b.fileName.toString())
                    }
                }
                    .map { buildNode(it) }
                    .toList()
            }

        } else {
            mutableListOf<FileTreeNode>()
        }

        return FileTreeNode(
            name = path.fileName.toString(),
            path = path.pathString,
            isDir = isDir,
            size = path.fileSize(),
            children = children
        )
    }
}