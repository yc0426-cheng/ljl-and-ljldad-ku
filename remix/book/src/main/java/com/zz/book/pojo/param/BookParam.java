package com.zz.book.pojo.param;

import lombok.Data;

/**
 * <p><b>模块-类说明</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 15:31
 */
@Data
public class BookParam {
    /**
     * id
     */
    private Long id;

    /**
     * 书名
     */
    private String title;

    /**
     * 上传者
     */
    private Long uploaderId;
}
