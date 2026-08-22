package com.zz.system.rag.dto;

import lombok.Data;

/**
 * <p><b>系统服务-问答请求体</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Data
public class RagQueryRequest {

    /**
     * 问题
     */
    private String question;
}
