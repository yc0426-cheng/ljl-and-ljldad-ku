package com.zz.book.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.book.enums.BookStatusEnum;
import com.zz.book.enums.PageTypeEnum;
import com.zz.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 书籍分页表
 *
 * @author yangcheng
 * @since 2026-09-17 15:19:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("book_page")
public class BookPage extends BaseEntity {

    /**
     * 分页记录主键ID
     */
    @TableId
    private Long bookPageId;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 书籍版本
     */
    private Integer bookVersion;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 导出排序号
     */
    private Integer sortOrder;

    /**
     * 文本内容类型
     */
    private PageTypeEnum contentType;

    /**
     * oss key
     */
    private String oss_key;

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

    /**
     * 该页字符数
     */
    private Integer charCount;

    /**
     * 状态
     */
    private BookStatusEnum status;
}
