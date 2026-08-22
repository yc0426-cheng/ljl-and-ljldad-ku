package com.zz.system.rag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * <p><b>系统服务-问答参考来源</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Data
public class SourceVO {

    /**
     * 来源文件名
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 引用片段
     */
    private String text;

    /**
     * 相似度分数
     */
    private Double score;
}
