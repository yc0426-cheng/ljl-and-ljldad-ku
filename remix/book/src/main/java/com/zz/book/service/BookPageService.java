package com.zz.book.service;

import com.zz.book.entity.BookPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.book.pojo.vo.BookPageVO;

/**
 * 针对表【book_page(书籍分页表)】的数据库操作Service
 *
 * @author yangcheng
 * @since 2026-09-17 15:19:43
 */
public interface BookPageService extends IService<BookPage> {

    /**
     * 浏览书籍
     */
    BookPageVO catBook();

    /**
     * 获取页码数据
     *
     * @param bookId 书id
     * @return 书籍分页结果
     */
    BookPageVO getPageBook(Long bookId);
}
