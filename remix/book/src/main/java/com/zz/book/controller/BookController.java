package com.zz.book.controller;

import com.zz.book.pojo.dto.BookDTO;
import com.zz.book.pojo.param.BookParam;
import com.zz.book.pojo.vo.BookVO;
import com.zz.book.service.BookService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p><b>书籍服务-控制器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/18 12:19
 */
@Slf4j
@RestController("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 查看书籍信息
     *
     * @param id id
     * @return 书籍返回结果类
     */
    @PostMapping("/list")
    public BookVO list(Long id){
        return bookService.list(id);
    }

    /**
     * 新增书籍(设计书籍)
     *
     * @param dto 新增书籍数据
     */
    @PostMapping("/add")
    public void add(BookDTO dto){
        bookService.add(dto);
    }

    /**
     * 删除书籍
     *
     * @param id 待删除书籍id
     */
    @PostMapping("/delete")
    public void delete(Long id) {
        bookService.delete(id);
    }

    /**
     * 修改书籍(设计书籍)
     *
     * @param dto 修改书籍数据
     */
    @PostMapping("/edit")
    void edit(BookDTO dto) {
        bookService.edit(dto);
    }

    /**
     * 上传书籍
     *
     * @param file 文件
     * @param userId 用户id
     */
    @PostMapping("/upload")
    void uploadBook(MultipartFile file, Long userId) {
        bookService.uploadBook(file,userId);
    }

    /**
     * 导出书籍
     *
     * @param response 请求
     * @param param 待导出数据
     */
    @PostMapping("/export")
    void exportBook(HttpServletResponse response, BookParam param) {
        bookService.exportBook(response,param);
    }
}
