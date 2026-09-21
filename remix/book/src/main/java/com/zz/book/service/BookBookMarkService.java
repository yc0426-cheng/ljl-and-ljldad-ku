package com.zz.book.service;

import com.zz.book.entity.BookBookMark;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 针对表【book_book_mark(书籍页签表)】的数据库操作Service
*
* @author yangcheng
* @since 2026-09-21 14:37:36
*/
public interface BookBookMarkService extends IService<BookBookMark> {

    /**
     * 获取最后查看的页码
     *
     * @param bookId 书籍id
     * @return 最后查看的页码
     */
    Integer getLastPageNoByBookId(Long bookId);
}
