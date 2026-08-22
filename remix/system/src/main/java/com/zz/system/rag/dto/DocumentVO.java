package com.zz.system.rag.dto;

import lombok.Data;

/**
 * <p><b>系统服务-文档登记信息</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Data
public class DocumentVO {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件类型: pdf, md, txt, docx
     */
    private String fileType;

    /**
     * 处理状态: 0-已上传, 1-处理中, 2-已索引, 3-失败
     */
    private Integer status;

    /**
     * 元数据（JSON格式）
     */
    private String metaData;
}
