package com.zz.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.api.file.avatar.BookFeignClient;
import com.zz.book.convert.BookConverter;
import com.zz.book.entity.Book;
import com.zz.book.entity.BookPage;
import com.zz.book.pojo.dto.BookDTO;
import com.zz.book.pojo.param.BookParam;
import com.zz.book.pojo.vo.BookPageVO;
import com.zz.book.pojo.vo.BookVO;
import com.zz.book.service.BookPageService;
import com.zz.book.service.BookService;
import com.zz.book.mapper.BookMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 针对表【book(书籍信息表)】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-09-17 15:05:35
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl extends ServiceImpl<BookMapper, Book>
        implements BookService {

    private final BookPageService bookPageService;

    private final BookConverter bookConverter;

    private final BookFeignClient bookFeignClient;
    private final BookService bookService;

    @Override
    public BookVO list(Long id) {
        return bookConverter.entityToVO(super.getById(id));
    }

    @Override
    public void add(BookDTO dto) {
        Book book = bookConverter.dtoToEntity(dto);
        // todo 补充信息
        super.save(book);
    }

    @Override
    public void delete(Long id) {
        super.removeById(id);
    }

    @Override
    public void edit(BookDTO dto) {

    }

    @Override
    public void uploadBook(MultipartFile file, Long userId) {
        // 上传至oss
        bookFeignClient.uploadBook(file, userId);
        //  todo 解析书结构，写入page_book
    }

    @Override
    public void exportBook(HttpServletResponse response, BookParam param) {
        Book book = super.getById(param.getId());
        // 获取对应书籍分页数据
        BookPageVO bookPage =  bookPageService.getPageBook(param.getId());
    }
}




