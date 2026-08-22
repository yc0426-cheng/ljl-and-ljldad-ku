package com.zz.system.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zz.system.rag.entity.RagDocument;
import com.zz.system.rag.mapper.RagDocumentMapper;
import com.zz.system.rag.service.RagDocumentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p><b>系统服务-针对表【rag_document】的数据库操作Service实现</b></p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Service
public class RagDocumentServiceImpl extends ServiceImpl<RagDocumentMapper, RagDocument>
        implements RagDocumentService {

    @Override
    public List<RagDocument> listByUser(Long userId) {
        LambdaQueryWrapper<RagDocument> qw = new LambdaQueryWrapper<>();
        qw.eq(RagDocument::getUserId, userId)
                .eq(RagDocument::getDelFlag, false)
                .orderByDesc(RagDocument::getId);
        return baseMapper.selectList(qw);
    }

    @Override
    public RagDocument markIndexed(Long userId, String fileName, String filePath, String fileType, String metaData) {
        LambdaQueryWrapper<RagDocument> qw = new LambdaQueryWrapper<>();
        qw.eq(RagDocument::getUserId, userId)
                .eq(RagDocument::getFileName, fileName)
                .eq(RagDocument::getDelFlag, false)
                .last("LIMIT 1");
        RagDocument existing = baseMapper.selectOne(qw);
        if (existing != null) {
            existing.setFilePath(filePath);
            existing.setFileType(fileType);
            existing.setMetaData(metaData);
            existing.setStatus(2);
            existing.setDelFlag(false);
            updateById(existing);
            return existing;
        }
        RagDocument doc = new RagDocument();
        doc.setUserId(userId);
        doc.setFileName(fileName);
        doc.setFilePath(filePath);
        doc.setFileType(fileType);
        doc.setMetaData(metaData);
        doc.setStatus(2);
        doc.setDelFlag(false);
        save(doc);
        return doc;
    }

    @Override
    public void deleteByUserAndName(Long userId, String fileName) {
        LambdaQueryWrapper<RagDocument> qw = new LambdaQueryWrapper<>();
        qw.eq(RagDocument::getUserId, userId).eq(RagDocument::getFileName, fileName);
        baseMapper.delete(qw);
    }
}
