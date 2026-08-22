package com.zz.system.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p><b>系统服务-RAG文档源表</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Data
@TableName("rag_document")
public class RagDocument {

    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
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
     * 文件存储路径（本地路径或S3 URL）
     */
    private String filePath;

    /**
     * 文件类型: pdf, md, txt, docx
     */
    private String fileType;

    /**
     * 处理状态: 0-已上传, 1-处理中, 2-已索引, 3-失败
     */
    private Integer status;

    /**
     * 元数据（JSON格式），如作者、页数、文件大小等
     */
    private String metaData;

    /**
     * 删除标记
     */
    private Boolean delFlag;
}
