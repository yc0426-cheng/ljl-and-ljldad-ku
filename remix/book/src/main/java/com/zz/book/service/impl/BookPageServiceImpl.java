package com.zz.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.book.convert.BookPageConverter;
import com.zz.book.entity.BookPage;
import com.zz.book.pojo.vo.BookPageVO;
import com.zz.book.service.BookPageService;
import com.zz.book.mapper.BookPageMapper;
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

    private final BookPageConverter bookPageConverter;

    @Override
    public List<BookPageVO> getPageBook(Long bookId) {
        LambdaQueryWrapper<BookPage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookPage::getBookId, bookId);
        List<BookPage> bookPageList = super.list(queryWrapper);
        return bookPageConverter.entity2Vo(bookPageList);
    }

    @Override
    public BookPageVO catBook(Long bookId) {
        return null;
    }
}




