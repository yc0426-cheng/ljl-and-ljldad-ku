package com.zz.book.service;

import com.zz.book.entity.BookPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.book.pojo.vo.BookOpenVO;
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
     * 获取书籍页码数据
     *
     * @param bookId 书id
     * @return 书籍分页结果
     */
    List<BookPageVO> getPageBook(Long bookId);

    /**
     * 翻开书籍
     *
     * @param bookId 书籍id
     * @return 返回书籍信息 + 默认首页内容/若是打开过则是打开的页面  + 总页数
     */
    BookOpenVO openBook(Long bookId);

    /**
     * 翻页
     *
     * @param bookId 书籍id
     * @param pageNo 页码
     * @return 页书据
     */
    BookPageVO readPage(Long bookId, Integer pageNo);

    /**
     * 定位
     *
     * @param bookId     书籍id
     * @param charOffset 字符偏移量
     * @return 定位数据
     */
    BookPageVO locatePage(Long bookId, int charOffset);
}
