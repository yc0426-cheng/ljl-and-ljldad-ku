package com.zz.book.pojo.vo;

import lombok.Data;

/**
 * <p><b>书籍服务-书籍分页返回结果类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 15:21
 */
@Data
public class BookPageVO {

    /**
     * 分页记录主键ID
     */
    private Long bookPageId;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 该页渲染资源OSS地址
     */
    private String contentUrl;

    /**
     * 该页纯文本内容
     */
    private String contentText;

    /**
     * 该页文本在全书中的起始字符偏移
     */
    private Integer charStart;

    /**
     * 该页文本在全书中的结束字符偏移
     */
    private Integer charEnd;
}
