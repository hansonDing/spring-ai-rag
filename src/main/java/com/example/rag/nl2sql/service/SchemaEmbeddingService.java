package com.example.rag.nl2sql.service;

import com.example.rag.nl2sql.entity.TableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 表描述向量化服务
 * 将表描述信息向量化存储到ES，用于语义检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaEmbeddingService {
    
    private final ElasticsearchVectorStore vectorStore;
    private final TableSchemaService tableSchemaService;
    
    // 索引名称
    private static final String INDEX_NAME = "nl2sql-table-schemas";
    
    /**
     * 将表信息添加到向量存储
     */
    public void addTableEmbedding(TableInfo tableInfo) {
        String embeddingText = tableInfo.toEmbeddingText();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tableId", tableInfo.getId());
        metadata.put("tableName", tableInfo.getTableName());
        metadata.put("tableAlias", tableInfo.getTableAlias());
        metadata.put("dbType", tableInfo.getDbType());
        metadata.put("databaseName", tableInfo.getDatabaseName());
        metadata.put("description", tableInfo.getDescription());
        
        Document document = new Document(
            tableInfo.getId(),
            embeddingText,
            metadata
        );
        
        vectorStore.add(List.of(document));
        log.info("Table embedding added: {} ({})", tableInfo.getTableName(), tableInfo.getId());
    }
    
    /**
     * 批量添加表信息到向量存储
     */
    public void addTableEmbeddings(List<TableInfo> tableInfos) {
        List<Document> documents = tableInfos.stream()
                .map(this::convertToDocument)
                .toList();
        
        vectorStore.add(documents);
        log.info("Batch table embeddings added: {} tables", tableInfos.size());
    }
    
    /**
     * 将表信息转换为Document
     */
    private Document convertToDocument(TableInfo tableInfo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tableId", tableInfo.getId());
        metadata.put("tableName", tableInfo.getTableName());
        metadata.put("tableAlias", tableInfo.getTableAlias());
        metadata.put("dbType", tableInfo.getDbType());
        metadata.put("databaseName", tableInfo.getDatabaseName());
        metadata.put("description", tableInfo.getDescription());
        
        return new Document(
            tableInfo.getId(),
            tableInfo.toEmbeddingText(),
            metadata
        );
    }
    
    /**
     * 搜索相关表（语义检索）
     */
    public List<TableInfo> searchRelevantTables(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        
        List<TableInfo> tables = new ArrayList<>();
        for (Document doc : documents) {
            String tableId = (String) doc.getMetadata().get("tableId");
            if (tableId != null) {
                TableInfo table = tableSchemaService.getTableById(tableId);
                if (table != null) {
                    tables.add(table);
                }
            }
        }
        
        log.debug("Found {} relevant tables for query: {}", tables.size(), query);
        return tables;
    }
    
    /**
     * 搜索相关表并返回带分数的结果
     */
    public List<TableSearchResult> searchRelevantTablesWithScore(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        
        List<TableSearchResult> results = new ArrayList<>();
        for (Document doc : documents) {
            String tableId = (String) doc.getMetadata().get("tableId");
            Double score = (Double) doc.getMetadata().get("distance");
            if (tableId != null) {
                TableInfo table = tableSchemaService.getTableById(tableId);
                if (table != null) {
                    results.add(new TableSearchResult(table, score != null ? score : 0.0));
                }
            }
        }
        
        return results;
    }
    
    /**
     * 删除表的向量嵌入
     */
    public void deleteTableEmbedding(String tableId) {
        vectorStore.delete(List.of(tableId));
        log.info("Table embedding deleted: {}", tableId);
    }
    
    /**
     * 更新表的向量嵌入
     */
    public void updateTableEmbedding(TableInfo tableInfo) {
        // 先删除旧的
        deleteTableEmbedding(tableInfo.getId());
        // 再添加新的
        addTableEmbedding(tableInfo);
        log.info("Table embedding updated: {}", tableInfo.getTableName());
    }
    
    /**
     * 获取所有已向量化的表ID
     */
    public List<String> getAllEmbeddedTableIds() {
        // 通过搜索空字符串获取所有文档
        SearchRequest searchRequest = SearchRequest.builder()
                .query("")
                .topK(10000)
                .build();
        
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return documents.stream()
                .map(doc -> (String) doc.getMetadata().get("tableId"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
    
    /**
     * 表搜索结果
     */
    public record TableSearchResult(TableInfo table, double score) {}
}
