package com.zz.book.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zz.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
* 书籍页签表
*
* @author yangcheng
* @since 2026-09-21 14:37:36
*/
@EqualsAndHashCode(callSuper = false)
@Data
@TableName("book_book_mark")
public class BookBookMark extends BaseEntity {

    /**
    * 页签ID
    */
    @TableId
    private Long bookBookMarkId;
    /**
    * 书籍ID
    */
    private Long bookId;
    /**
    * 所属用户ID
    */
    private Long userId;
    /**
    * 定位信息JSON
    */
    private Object location;
    /**
    * 页码
    */
    private Integer pageNo;
    /**
    * 最后浏览页码
    */
    private Integer lastPageNo;
    /**
    * 页签名称
    */
    private String label;
    /**
    * 页签颜色
    */
    private String color;
    /**
    * 创建用户ID
    */
    private Long createUser;
    /**
    * 创建时间
    */
    private Date createTime;
    /**
    * 更新用户ID
    */
    private Long updateUser;
    /**
    * 更新时间
    */
    private Date updateTime;
}
