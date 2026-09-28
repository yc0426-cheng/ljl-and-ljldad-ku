package com.zz.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zz.api.file.avatar.FileFeignClient;
import com.zz.book.convert.BookConverter;
import com.zz.book.entity.Book;
import com.zz.book.enums.BookExceptionEnum;
import com.zz.book.pojo.dto.BookDTO;
import com.zz.book.pojo.param.BookParam;
import com.zz.book.pojo.vo.BookPageVO;
import com.zz.book.pojo.vo.BookVO;
import com.zz.book.service.BookPageService;
import com.zz.book.service.BookService;
import com.zz.book.mapper.BookMapper;
import com.zz.common.core.constant.RedisKeyConstant;
import com.zz.common.core.context.LoginUserHolder;
import com.zz.common.core.exception.BizException;
import com.zz.common.redis.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 针对表【book(书籍信息表)】的数据库操作Service实现
 *
 * @author yangcheng
 * @since 2026-09-17 15:05:35
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl extends ServiceImpl<BookMapper, Book>
        implements BookService {

    private final BookPageService bookPageService;

    private final BookConverter bookConverter;

    private final FileFeignClient bookFeignClient;

    private final FileFeignClient fileFeignClient;

    private final RedisService redisService;

    @Override
    public BookVO list(Long id) {
        return bookConverter.entityToVO(super.getById(id));
    }

    @Override
    public void add(BookDTO dto) {
        Book book = bookConverter.dtoToEntity(dto);
        // 补充信息
        book.setStatus(1);
        super.save(book);

        // 新增分页参数

        // 将 书名 存入 redis
        redisService.getRedisTemplate()
                .opsForValue()
                .set(
                        RedisKeyConstant.BOOK_USER_PREFIX
                                + LoginUserHolder.get().getUserId()
                                + ","
                                + getIdByTitle(book.getTitle()),
                        book.getTitle()
                );
    }

    @Override
    public void delete(Long id) {
        super.removeById(id);
    }

    @Override
    public void edit(BookDTO dto) {
        // 判断是否是自己的书籍 自己的书籍才允许编辑
        String title = redisService.getRedisTemplate()
                .opsForValue()
                .get(RedisKeyConstant.BOOK_USER_PREFIX + LoginUserHolder.get().getUserId()+ ","
                        + getIdByTitle(dto.getTitle()));
        if ( title == null ){
            throw new BizException(BookExceptionEnum.BOOK_NOT_AUTHORIZATION);
        }
        // 修改书籍基本信息

        // 根据书名拿到id，获取书籍分页信息

        // 然后根据修改位置插入分页书籍
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
        if (book == null) {
            throw new BizException(BookExceptionEnum.BOOK_NOT_EXIST);
        }
        // 获取对应书籍分页数据
        List<BookPageVO> bookPageList = bookPageService.getPageBook(param.getId());
        // 3. 设置响应头
        String fileName = book.getTitle() + ".zip";
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);

        // 4. 流式写入 ZIP
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            // 4.1 写入全书纯文本
            zos.putNextEntry(new ZipEntry("content.txt"));
            for (BookPageVO page : bookPageList) {
                if (page.getContentText() != null) {
                    zos.write((page.getContentText() + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
            zos.closeEntry();

            // 4.2 逐页下载 OSS 资源
            for (BookPageVO page : bookPageList) {
                if (page.getContentUrl() == null || page.getContentUrl().isBlank()) {
                    continue;
                }
                String entryName = "pages/page_" + page.getPageNo() + ".png";
                zos.putNextEntry(new ZipEntry(entryName));

                try {
                    fileFeignClient.downloadOssToStream(page.getContentUrl(), zos);
                } catch (Exception e) {
                    log.warn("第{}页资源下载失败: {}", page.getPageNo(), page.getContentUrl(), e);
                    zos.write(("第" + page.getPageNo() + "页资源缺失").getBytes(StandardCharsets.UTF_8));
                }
                zos.closeEntry();
            }

            zos.finish();

        } catch (IOException e) {
            throw new BizException(BookExceptionEnum.BOOK_EXPORT_FAIL);
        }
    }

    /**
     * 通过书名获取id
     *
     * @param title 书名
     * @return bookId
     */
    private Long getIdByTitle(String title) {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getTitle, title);
        return super.getOne(queryWrapper).getId();
    }
}




