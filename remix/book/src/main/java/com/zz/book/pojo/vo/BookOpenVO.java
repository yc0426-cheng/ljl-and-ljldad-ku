package com.zz.book.pojo.vo;

import lombok.Data;

/**
 * <p><b>书籍服务-首页信息返回结果类</b></p>
 *
 * @author yangcheng
 * @since 2026/9/21 13:53
 */
@Data
public class BookOpenVO {

    /**
     * 书籍元信息
     */
    private BookVO book;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 默认首页内容（后续页按 pageNo 懒加载）
     */
    private BookPageVO pageNo;
}
