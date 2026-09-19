package com.zz.book.service;

import com.zz.book.entity.BookPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.book.pojo.vo.BookPageVO;

import java.util.List;

/**
 * 针对表【book_page(书籍分页表)】的数据库操作Service
 *
 * @author yangcheng
 * @since 2026-09-17 15:19:43
 */
public interface BookPageService extends IService<BookPage> {

    /**
     * 浏览书籍
     *
     * @param bookId 书籍id
     */
    BookPageVO catBook(Long bookId);

    /**
     * 获取书籍页码数据
     *
     * @param bookId 书id
     * @return 书籍分页结果
     */
    List<BookPageVO> getPageBook(Long bookId);
}
