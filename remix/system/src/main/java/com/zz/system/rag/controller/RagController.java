package com.zz.system.rag.controller;

import com.zz.common.core.result.Result;
import com.zz.system.rag.dto.DocumentVO;
import com.zz.system.rag.dto.QueryVO;
import com.zz.system.rag.dto.RagQueryRequest;
import com.zz.system.rag.dto.ReloadVO;
import com.zz.system.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p><b>系统服务-RAG 文档与问答接口</b></p>
 *
 * <p>所有接口都需登录令牌（由 TokenAuthFilter 校验），当前用户取自登录态。</p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * 基于个人知识库问答
     */
    @PostMapping("/query")
    public Result<QueryVO> query(@RequestBody RagQueryRequest request) {
        return Result.success(ragService.query(request.getQuestion()));
    }

    /**
     * 上传 .md 文档并索引
     */
    @PostMapping("/documents")
    public Result<DocumentVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(ragService.upload(file));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/documents/{fileName}")
    public Result<Void> delete(@PathVariable String fileName) {
        ragService.delete(fileName);
        return Result.success();
    }

    /**
     * 当前用户文档列表
     */
    @GetMapping("/documents")
    public Result<List<DocumentVO>> list() {
        return Result.success(ragService.list());
    }

    /**
     * 重新扫描文档目录并更新索引
     */
    @PostMapping("/documents/reload")
    public Result<ReloadVO> reload() {
        return Result.success(ragService.reload());
    }
}
