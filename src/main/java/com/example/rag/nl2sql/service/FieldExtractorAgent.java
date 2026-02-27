package com.example.rag.nl2sql.service;

import com.example.rag.nl2sql.entity.ColumnInfo;
import com.example.rag.nl2sql.entity.TableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段提取Agent (Agent 1)
 * 从大量字段中提取与用户查询相关的字段
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldExtractorAgent {
    
    private final ChatClient chatClient;
    
    @Value("classpath:/prompts/nl2sql/field-extractor-prompt.st")
    private Resource fieldExtractorPrompt;
    
    /**
     * 从候选表中提取相关字段
     * 
     * @param naturalQuery 自然语言查询
     * @param candidateTables 候选表列表（从向量检索获得）
     * @param dialect SQL方言
     * @return 提取的相关字段列表
     */
    public List<ColumnInfo> extractRelevantFields(String naturalQuery, List<TableInfo> candidateTables, String dialect) {
        if (candidateTables == null || candidateTables.isEmpty()) {
            log.warn("No candidate tables provided for field extraction");
            return new ArrayList<>();
        }
        
        log.info("Extracting relevant fields for query: {} from {} tables", naturalQuery, candidateTables.size());
        
        // 构建候选字段上下文
        String candidateFieldsContext = buildCandidateFieldsContext(candidateTables);
        
        // 构建系统提示词
        String systemPrompt = buildSystemPrompt(dialect);
        
        // 构建用户提示词
        String userPrompt = buildUserPrompt(naturalQuery, candidateFieldsContext);
        
        // 调用LLM
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userPrompt));
        
        Prompt prompt = new Prompt(messages);
        
        String response = chatClient.prompt(prompt)
                .call()
                .content();
        
        log.debug("Field extractor response: {}", response);
        
        // 解析响应，提取字段
        return parseExtractedFields(response, candidateTables);
    }
    
    /**
     * 构建候选字段上下文
     */
    private String buildCandidateFieldsContext(List<TableInfo> tables) {
        StringBuilder context = new StringBuilder();
        
        for (TableInfo table : tables) {
            context.append("\n=== 表: ").append(table.getTableName());
            if (table.getTableAlias() != null && !table.getTableAlias().isEmpty()) {
                context.append(" (").append(table.getTableAlias()).append(")");
            }
            context.append(" ===\n");
            context.append("描述: ").append(table.getDescription()).append("\n");
            context.append("字段:\n");
            
            if (table.getColumns() != null) {
                for (ColumnInfo col : table.getColumns()) {
                    context.append("  - ").append(col.toDescription()).append("\n");
                }
            }
            
            // 添加主键信息
            if (table.getPrimaryKey() != null) {
                context.append("主键: ").append(table.getPrimaryKey()).append("\n");
            }
            
            // 添加外键信息
            if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                context.append("外键关系:\n");
                table.getForeignKeys().forEach((key, ref) -> 
                    context.append("  - ").append(key).append(" -> ").append(ref).append("\n")
                );
            }
        }
        
        return context.toString();
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String dialect) {
        return String.format("""
            你是一个专业的数据库字段提取专家。你的任务是从候选表字段中，
            识别并提取与用户自然语言查询最相关的字段。
            
            规则：
            1. 仔细分析用户的查询意图
            2. 从候选表中选择最相关的字段
            3. 考虑字段的业务含义、数据类型和关联关系
            4. 必须包含用于JOIN操作的外键字段
            5. 优先选择有明确业务含义的字段
            6. 返回格式必须是JSON数组
            
            SQL方言: %s
            
            输出格式：
            {
              "reasoning": "简要说明选择这些字段的理由",
              "fields": [
                {
                  "tableName": "表名",
                  "columnName": "字段名",
                  "relevance": "high/medium/low",
                  "usage": "说明该字段在查询中的用途"
                }
              ]
            }
            """, dialect);
    }
    
    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(String naturalQuery, String candidateFieldsContext) {
        return String.format("""
            用户查询: %s
            
            候选表和字段信息:
            %s
            
            请分析用户查询，从候选字段中提取最相关的字段。
            只返回JSON格式的结果，不要包含其他解释文字。
            """, naturalQuery, candidateFieldsContext);
    }
    
    /**
     * 解析提取的字段
     */
    private List<ColumnInfo> parseExtractedFields(String response, List<TableInfo> candidateTables) {
        List<ColumnInfo> extractedFields = new ArrayList<>();
        
        try {
            // 提取JSON部分
            String jsonStr = extractJson(response);
            if (jsonStr == null) {
                log.warn("No JSON found in field extractor response");
                return extractedFields;
            }
            
            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> result = mapper.readValue(jsonStr, Map.class);
            
            List<Map<String, Object>> fields = (List<Map<String, Object>>) result.get("fields");
            if (fields == null) {
                return extractedFields;
            }
            
            // 构建表名到表信息的映射
            Map<String, TableInfo> tableMap = new HashMap<>();
            for (TableInfo table : candidateTables) {
                tableMap.put(table.getTableName(), table);
            }
            
            // 查找对应的字段信息
            for (Map<String, Object> fieldInfo : fields) {
                String tableName = (String) fieldInfo.get("tableName");
                String columnName = (String) fieldInfo.get("columnName");
                
                TableInfo table = tableMap.get(tableName);
                if (table != null && table.getColumns() != null) {
                    for (ColumnInfo col : table.getColumns()) {
                        if (col.getColumnName().equalsIgnoreCase(columnName)) {
                            extractedFields.add(col);
                            break;
                        }
                    }
                }
            }
            
            log.info("Extracted {} relevant fields", extractedFields.size());
            
        } catch (Exception e) {
            log.error("Failed to parse field extractor response: {}", e.getMessage(), e);
        }
        
        return extractedFields;
    }
    
    /**
     * 从响应中提取JSON
     */
    private String extractJson(String response) {
        // 尝试找到JSON对象
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return null;
    }
}
