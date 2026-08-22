package com.zz.system.rag.client;

import cn.hutool.core.util.StrUtil;
import com.zz.common.core.exception.BizException;
import com.zz.system.rag.dto.QueryVO;
import com.zz.system.rag.dto.RagDocumentResponse;
import com.zz.system.rag.dto.RagQueryRequest;
import com.zz.system.rag.dto.ReloadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

/**
 * <p><b>系统服务-RKBase（Python RAG 服务）客户端</b></p>
 *
 * <p>封装对 RKBase 用户级路由的调用：问答、上传、删除、重索引。
 * 所有调用失败统一转成 {@link BizException}，避免把底层异常抛给前端。</p>
 *
 * @author yangcheng
 * @since 2026-08-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagClient {

    private final RestClient ragRestClient;

    /**
     * 问答
     */
    public QueryVO query(Long userId, String question) {
        RagQueryRequest request = new RagQueryRequest();
        request.setQuestion(question);
        return call(() -> ragRestClient.post()
                .uri("/api/v1/users/{userId}/query", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(QueryVO.class));
    }

    /**
     * 上传 .md 文档并索引
     */
    public RagDocumentResponse upload(Long userId, String fileName, byte[] content) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        return call(() -> ragRestClient.post()
                .uri("/api/v1/users/{userId}/documents", userId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(RagDocumentResponse.class));
    }

    /**
     * 删除文档并重建索引；文档在 RKBase 侧已不存在时返回 null（幂等删除）
     */
    public RagDocumentResponse delete(Long userId, String fileName) {
        try {
            return ragRestClient.delete()
                    .uri("/api/v1/users/{userId}/documents/{fileName}", userId, fileName)
                    .retrieve()
                    .body(RagDocumentResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.info("RAG 文档在服务侧已不存在，按幂等删除处理: {}", fileName);
                return null;
            }
            throw handleError(e);
        } catch (RestClientException e) {
            throw handleError(e);
        }
    }

    /**
     * 重新扫描文档目录并更新索引
     */
    public ReloadVO reload(Long userId) {
        return call(() -> ragRestClient.post()
                .uri("/api/v1/users/{userId}/reload", userId)
                .retrieve()
                .body(ReloadVO.class));
    }

    /**
     * 统一执行并包装异常
     */
    private <T> T call(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RestClientException e) {
            throw handleError(e);
        }
    }

    /**
     * 把 RestClient 异常转成 BizException，保留具体原因
     */
    private BizException handleError(RestClientException e) {
        String message = "RAG 服务调用失败";
        if (e instanceof RestClientResponseException responseException) {
            String body = responseException.getResponseBodyAsString();
            if (StrUtil.isNotBlank(body)) {
                message = message + ": " + body;
            } else {
                message = message + ": HTTP " + responseException.getStatusCode().value();
            }
        } else {
            message = message + ": " + e.getMessage();
        }
        log.warn(message, e);
        return new BizException(message);
    }
}
