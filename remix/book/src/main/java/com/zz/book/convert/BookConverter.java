package com.zz.book.convert;

import com.zz.book.entity.Book;
import com.zz.book.pojo.dto.BookDTO;
import com.zz.book.pojo.vo.BookVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * <p><b>书籍服务-转换器</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 15:34
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookConverter {
    /**
     * 实体类转vo
     */
    BookVO entityToVO(Book book);

    /**
     * dto 转 entity
     */
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    Book dtoToEntity(BookDTO bookDTO);
}
