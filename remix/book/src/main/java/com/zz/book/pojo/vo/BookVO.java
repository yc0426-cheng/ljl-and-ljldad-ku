package com.zz.book.pojo.vo;

import lombok.Data;

/**
 * <p><b>书籍服务-book返回结果类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 15:15
 */
@Data
public class BookVO {

    /**
     * ID，全局唯一
     */
    private Long id;
    /**
     * 书名
     */
    private String title;
    /**
     * 封面图OSS地址
     */
    private String coverUrl;
    /**
     * 书籍原文件OSS地址
     */
    private String fileUrl;
    /**
     * 文件格式
     */
    private String fileFormat;
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    /**
     * 文件SHA-256
     */
    private String fileHash;
    /**
     * 总页数
     */
    private Integer totalPages;
    /**
     * 书籍语言
     */
    private String language;
    /**
     * 状态：1正常 0下架 2解析中 3解析失败
     */
    private Integer status;
    /**
     * 上传者ID
     */
    private Long uploaderId;
}
