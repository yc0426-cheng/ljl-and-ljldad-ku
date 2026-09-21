package com.zz.book.controller;

import com.zz.book.pojo.vo.BookOpenVO;
import com.zz.book.pojo.vo.BookPageVO;
import com.zz.book.service.BookPageService;
import com.zz.common.log.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    @OperationLog(value = "获取页码数据", model = OperationLog.LogModel.BOOK)
    @PostMapping("/get")
    List<BookPageVO> getPageBook(Long bookId) {
        return bookPageService.getPageBook(bookId);
    }

    /**
     * 翻开书籍
     *
     * @param bookId 书籍id
     * @return 返回书籍信息 + 首页内容 + 总页数
     */
    @OperationLog(value = "获取页码数据", model = OperationLog.LogModel.BOOK)
    @GetMapping("/{bookId}/open")
    public BookOpenVO open(@PathVariable Long bookId) {
        return bookPageService.openBook(bookId);
    }

    /**
     * 翻页
     *
     * @param bookId 书籍id
     * @param pageNo 页码
     * @return 页数据
     */
    @OperationLog(value = "翻页", model = OperationLog.LogModel.BOOK)
    @GetMapping("/{bookId}/pages/{pageNo}")
    public BookPageVO readPage(@PathVariable Long bookId, @PathVariable Integer pageNo) {
        return bookPageService.readPage(bookId, pageNo);
    }

    /**
     * 定位
     *
     * @param bookId 书籍id
     * @param charOffset 字符偏移量
     * @return 定位数据
     */
    @OperationLog(value = "定位", model = OperationLog.LogModel.BOOK)
    @GetMapping("/{bookId}/locate")
    public BookPageVO locate(@PathVariable Long bookId, @RequestParam int charOffset) {
        return bookPageService.locatePage(bookId, charOffset);
    }
}
