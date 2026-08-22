package com.zz.system.rag.dto;

import lombok.Data;

import java.util.List;

/**
 * <p><b>系统服务-问答响应体</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Data
public class QueryVO {

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 参考来源列表
     */
    private List<SourceVO> sources;
}
