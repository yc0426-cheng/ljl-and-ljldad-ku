package com.zz.book.convert;

import com.zz.book.entity.BookPage;
import com.zz.book.pojo.vo.BookPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * <p><b>书籍服务-转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 16:26
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookPageConverter {

    /**
     * 实体类转vo
     */
    BookPageVO entity2Vo(BookPage bookPage);
}
