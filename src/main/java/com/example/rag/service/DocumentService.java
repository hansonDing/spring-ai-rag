package com.example.rag.service;

import com.example.rag.entity.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final EmbeddingService embeddingService;

    /**
     * 保存文档并生成向量嵌入
     */
    public Document saveDocument(String title, String content, String source) {
        String id = UUID.randomUUID().toString();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", title);
        metadata.put("source", source);
        metadata.put("createdAt", LocalDateTime.now().toString());

        // 保存到向量数据库
        embeddingService.addDocument(id, content, metadata);

        Document document = Document.builder()
                .id(id)
                .title(title)
                .content(content)
                .source(source)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Document saved: {}", id);
        return document;
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String id) {
        embeddingService.deleteDocument(id);
        log.info("Document deleted: {}", id);
    }
}
