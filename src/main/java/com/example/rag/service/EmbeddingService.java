package com.example.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final ElasticsearchVectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    /**
     * 添加文档到向量数据库
     */
    public void addDocument(String id, String content, Map<String, Object> metadata) {
        Document document = new Document(id, content, metadata);
        vectorStore.add(List.of(document));
        log.info("Document added to vector store: {}", id);
    }

    /**
     * 搜索相似文档
     */
    public List<Document> searchSimilarDocuments(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        
        return vectorStore.similaritySearch(searchRequest);
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String id) {
        vectorStore.delete(List.of(id));
        log.info("Document deleted from vector store: {}", id);
    }
}
