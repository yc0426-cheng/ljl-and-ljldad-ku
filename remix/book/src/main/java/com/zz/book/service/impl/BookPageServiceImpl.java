package com.zz.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.book.convert.BookConverter;
import com.zz.book.convert.BookPageConverter;
import com.zz.book.entity.Book;
import com.zz.book.entity.BookPage;
import com.zz.book.enums.BookExceptionEnum;
import com.zz.book.pojo.vo.BookOpenVO;
import com.zz.book.pojo.vo.BookPageVO;
import com.zz.book.service.BookBookMarkService;
import com.zz.book.service.BookPageService;
import com.zz.book.mapper.BookPageMapper;
import com.zz.book.service.BookService;
import com.zz.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 针对表【book_page(书籍分页表)】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-09-17 15:19:43
 */
@Service
@RequiredArgsConstructor
public class BookPageServiceImpl extends ServiceImpl<BookPageMapper, BookPage>
        implements BookPageService {

    private final BookService bookService;

    private final BookBookMarkService bookBookMarkService;

    private final BookConverter bookConverter;

    private final BookPageConverter bookPageConverter;

    @Override
    public List<BookPageVO> getPageBook(Long bookId) {
        LambdaQueryWrapper<BookPage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookPage::getBookId, bookId);
        List<BookPage> bookPageList = super.list(queryWrapper);
        return bookPageConverter.entity2Vo(bookPageList);
    }

    @Override
    public BookOpenVO openBook(Long bookId) {
        // 获取书籍信息
        Book book = bookService.getById(bookId);
        if (book == null) {
            throw new BizException(BookExceptionEnum.BOOK_NOT_EXIST);
        }

        // 判断书籍状态
        if (book.getStatus() == null || book.getStatus() != 1) {
            throw new BizException(BookExceptionEnum.BOOK_STATUS_EXCEPTION);
        }

        // 获取分页信息
        LambdaQueryWrapper<BookPage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookPage::getBookId, bookId).eq(BookPage::getPageNo, 1);
        List<BookPage> pages = super.list(queryWrapper);

        // 拼接参数
        BookOpenVO vo = new BookOpenVO();
        vo.setBook(bookConverter.entityToVO(book));
        vo.setTotalPages(pages.size());
        // 判断是否是第一次打开此书
        Integer lastPageNo = bookBookMarkService.getLastPageNoByBookId(bookId);
        if (lastPageNo != null) {
            vo.setPageNo(bookPageConverter.entity2Vo(pages.get(lastPageNo - 1)));
        } else {
            vo.setPageNo(bookPageConverter.entity2Vo(pages.get(0)));
        }
        return vo;
    }

    @Override
    public BookPageVO readPage(Long bookId, Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            throw new BizException(BookExceptionEnum.BOOK_PAGE_ILLEGAL);
        }
        // 根据页码和书籍id获取此页的数据


        return null;
    }

    @Override
    public BookPageVO locatePage(Long bookId, int charOffset) {
        return null;
    }
}




