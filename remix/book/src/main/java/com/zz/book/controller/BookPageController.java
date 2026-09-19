package com.zz.book.controller;

import com.zz.book.pojo.vo.BookPageVO;
import com.zz.book.service.BookPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p><b>书籍服务-控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/18 12:34
 */
@Slf4j
@RestController("/book/page")
@RequiredArgsConstructor
public class BookPageController {

    private final BookPageService bookPageService;

    /**
     * 获取页码数据
     *
     * @param bookId 书id
     * @return 书籍分页结果
     */
    @PostMapping("/get")
    List<BookPageVO> getPageBook(Long bookId) {
        return bookPageService.getPageBook(bookId);
    }

    /**
     * 浏览书籍
     *
     * @param bookId 书籍id
     */
    @PostMapping("/cat")
    BookPageVO catBook(Long bookId) {
        return bookPageService.catBook(bookId);
    }
}
