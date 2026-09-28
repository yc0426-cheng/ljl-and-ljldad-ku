package com.zz.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.book.entity.BookBookMark;
import com.zz.book.service.BookBookMarkService;
import com.zz.book.mapper.BookBookMarkMapper;
import org.springframework.stereotype.Service;

/**
 * 针对表【book_book_mark(书籍页签表)】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-09-21 14:37:36
 */
@Service
public class BookBookMarkServiceImpl extends ServiceImpl<BookBookMarkMapper, BookBookMark>
        implements BookBookMarkService {

    @Override
    public Integer getLastPageNoByBookId(Long bookId) {
        LambdaQueryWrapper<BookBookMark> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookBookMark::getBookId, bookId);
        BookBookMark bookBookMark = super.getOne(queryWrapper);
        return bookBookMark == null ? 0 : bookBookMark.getPageNo();
    }
}




