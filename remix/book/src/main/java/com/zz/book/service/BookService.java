package com.zz.book.service;

import com.zz.book.entity.Book;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zz.book.pojo.dto.BookDTO;
import com.zz.book.pojo.param.BookParam;
import com.zz.book.pojo.vo.BookVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
* 针对表【book(书籍信息表)】的数据库操作Service
*
* @author yangcheng
* @since 2026-09-17 15:05:35
*/
public interface BookService extends IService<Book> {

    /**
     * 查看书籍信息
     */
    BookVO list(Long id);

    /**
     * 新增书籍(设计书籍)
     */
    void add(BookDTO dto);

    /**
     * 删除书籍
     */
    void delete(Long id);

    /**
     * 修改书籍(设计书籍)
     */
    void edit(BookDTO dto);

    /**
     * 上传书籍
     */
    void uploadBook(MultipartFile file, Long userId);

    /**
     * 导出书籍
     */
    void exportBook(HttpServletResponse response, BookParam param);
}
