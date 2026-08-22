package com.zz.system.rag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * <p><b>系统服务-重新索引响应体</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Data
public class ReloadVO {

    /**
     * 提示信息
     */
    private String message;

    /**
     * 当前索引的文档分块总数
     */
    @JsonProperty("total_chunks")
    private Integer totalChunks;
}
