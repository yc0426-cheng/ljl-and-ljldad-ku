package com.zz.book.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
* 书籍信息表
*
* @author yangcheng
* @since 2026-09-17 15:05:35
*/
@EqualsAndHashCode(callSuper = false)
@Data
@TableName("book")
public class Book extends BaseEntity {

    /**
    * ID，全局唯一
    */
    @TableId
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
