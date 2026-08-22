package com.zz.system.rag.service;

import cn.hutool.core.util.StrUtil;
import com.zz.common.core.context.LoginUserHolder;
import com.zz.common.core.exception.BizException;
import com.zz.common.core.pojo.LoginUserInfo;
import com.zz.system.rag.client.RagClient;
import com.zz.system.rag.dto.DocumentVO;
import com.zz.system.rag.dto.QueryVO;
import com.zz.system.rag.dto.RagDocumentResponse;
import com.zz.system.rag.dto.ReloadVO;
import com.zz.system.rag.entity.RagDocument;
import com.zz.system.rag.enums.RagExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p><b>系统服务-RAG 业务逻辑</b></p>
 *
 * <p>从登录态取当前用户，委托 {@link RagClient} 调用 RKBase，
 * 并维护 {@code rag_document} 登记表（上传登记、删除清理、列表查询）。</p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Service
@RequiredArgsConstructor
public class RagService {

    private final RagClient ragClient;
    private final RagDocumentService ragDocumentService;

    /**
     * 问答
     */
    public QueryVO query(String question) {
        if (StrUtil.isBlank(question)) {
            throw new BizException("问题不能为空");
        }
        return ragClient.query(currentUserId(), question.trim());
    }

    /**
     * 上传文档：调 RKBase 索引成功后登记到数据库
     */
    public DocumentVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(RagExceptionEnum.FILE_EMPTY_ERROR);
        }
        String fileName = file.getOriginalFilename();
        if (StrUtil.isBlank(fileName) || !fileName.toLowerCase().endsWith(".md")) {
            throw new BizException(RagExceptionEnum.FILE_TYPE_ERROR);
        }
        Long userId = currentUserId();
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BizException("文件读取失败: " + e.getMessage());
        }
        RagDocumentResponse response = ragClient.upload(userId, fileName, content);
        String filePath = "data/" + userId + "/" + fileName;
        String metaData = "{\"totalChunks\":" + response.getTotalChunks() + "}";
        RagDocument document = ragDocumentService.markIndexed(userId, fileName, filePath, "md", metaData);
        return toDocumentVO(document);
    }

    /**
     * 删除文档：先删 RKBase 侧索引，再清理数据库登记
     */
    public void delete(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            throw new BizException(RagExceptionEnum.FILE_TYPE_ERROR);
        }
        Long userId = currentUserId();
        ragClient.delete(userId, fileName);
        ragDocumentService.deleteByUserAndName(userId, fileName);
    }

    /**
     * 当前用户文档列表（来自数据库登记表）
     */
    public List<DocumentVO> list() {
        Long userId = currentUserId();
        return ragDocumentService.listByUser(userId).stream()
                .map(this::toDocumentVO)
                .toList();
    }

    /**
     * 重新扫描并更新索引
     */
    public ReloadVO reload() {
        return ragClient.reload(currentUserId());
    }

    /**
     * 从登录态获取当前用户id
     */
    private Long currentUserId() {
        LoginUserInfo loginUserInfo = LoginUserHolder.get();
        if (loginUserInfo == null || loginUserInfo.getUserId() == null) {
            throw new BizException(RagExceptionEnum.NOT_LOGIN);
        }
        return loginUserInfo.getUserId();
    }

    /**
     * 数据库记录转前端 VO
     */
    private DocumentVO toDocumentVO(RagDocument document) {
        DocumentVO vo = new DocumentVO();
        vo.setId(document.getId());
        vo.setUserId(document.getUserId());
        vo.setFileName(document.getFileName());
        vo.setFileType(document.getFileType());
        vo.setStatus(document.getStatus());
        vo.setMetaData(document.getMetaData());
        return vo;
    }
}
