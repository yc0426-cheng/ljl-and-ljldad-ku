package com.zz.system.rag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * <p><b>系统服务-RKBase 上传/删除文档的响应体</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Data
public class RagDocumentResponse {

    /**
     * 文件名
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 本次操作涉及的分块数
     */
    @JsonProperty("total_chunks")
    private Integer totalChunks;
}
