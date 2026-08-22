package com.zz.system.rag.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.zz.system.rag.entity.RagDocument;

import java.util.List;

/**
 * <p><b>系统服务-针对表【rag_document】的数据库操作Service</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
public interface RagDocumentService extends IService<RagDocument> {

    /**
     * 查询某用户未删除的文档列表
     *
     * @param userId 用户id
     * @return 文档列表
     */
    List<RagDocument> listByUser(Long userId);

    /**
     * 幂等登记：该用户已存在同名文档则更新为已索引，否则新增
     *
     * @param userId   用户id
     * @param fileName 文件名
     * @param filePath 存储路径
     * @param fileType 文件类型
     * @param metaData 元数据JSON
     * @return 登记后的文档记录
     */
    RagDocument markIndexed(Long userId, String fileName, String filePath, String fileType, String metaData);

    /**
     * 删除某用户的文档登记
     *
     * @param userId   用户id
     * @param fileName 文件名
     */
    void deleteByUserAndName(Long userId, String fileName);
}
