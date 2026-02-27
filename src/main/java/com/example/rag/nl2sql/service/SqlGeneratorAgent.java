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

import java.util.*;

/**
 * SQL生成Agent (Agent 2)
 * 基于精简的字段上下文生成SQL语句
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlGeneratorAgent {
    
    private final ChatClient chatClient;
    
    @Value("classpath:/prompts/nl2sql/sql-generator-prompt.st")
    private Resource sqlGeneratorPrompt;
    
    /**
     * 生成SQL语句
     * 
     * @param naturalQuery 自然语言查询
     * @param relevantTables 相关表列表
     * @param relevantFields 相关字段列表（由Agent 1提取）
     * @param dialect SQL方言
     * @return 生成的SQL语句
     */
    public SqlGenerationResult generateSql(String naturalQuery, List<TableInfo> relevantTables, 
                                            List<ColumnInfo> relevantFields, String dialect) {
        log.info("Generating SQL for query: {} with dialect: {}", naturalQuery, dialect);
        
        // 构建精简的上下文
        String context = buildContext(relevantTables, relevantFields);
        
        // 构建系统提示词
        String systemPrompt = buildSystemPrompt(dialect);
        
        // 构建用户提示词
        String userPrompt = buildUserPrompt(naturalQuery, context);
        
        // 调用LLM
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userPrompt));
        
        Prompt prompt = new Prompt(messages);
        
        String response = chatClient.prompt(prompt)
                .call()
                .content();
        
        log.debug("SQL generator response: {}", response);
        
        // 解析响应
        return parseSqlResponse(response);
    }
    
    /**
     * 构建精简的上下文
     */
    private String buildContext(List<TableInfo> tables, List<ColumnInfo> fields) {
        StringBuilder context = new StringBuilder();
        
        // 按表分组字段
        Map<String, List<ColumnInfo>> fieldsByTable = new HashMap<>();
        for (ColumnInfo field : fields) {
            fieldsByTable.computeIfAbsent(field.getTableId(), k -> new ArrayList<>()).add(field);
        }
        
        // 构建表信息映射
        Map<String, TableInfo> tableMap = new HashMap<>();
        for (TableInfo table : tables) {
            tableMap.put(table.getId(), table);
        }
        
        // 生成上下文
        for (Map.Entry<String, List<ColumnInfo>> entry : fieldsByTable.entrySet()) {
            String tableId = entry.getKey();
            List<ColumnInfo> tableFields = entry.getValue();
            TableInfo table = tableMap.get(tableId);
            
            if (table != null) {
                context.append("\n表: ").append(table.getTableName());
                if (table.getTableAlias() != null && !table.getTableAlias().isEmpty()) {
                    context.append(" (").append(table.getTableAlias()).append(")");
                }
                context.append("\n");
                context.append("描述: ").append(table.getDescription()).append("\n");
                context.append("相关字段:\n");
                
                for (ColumnInfo field : tableFields) {
                    context.append("  - ").append(field.getColumnName());
                    context.append(" ").append(field.getDataType());
                    if (field.getDescription() != null && !field.getDescription().isEmpty()) {
                        context.append(" (").append(field.getDescription()).append(")");
                    }
                    if (field.getIsPrimaryKey() != null && field.getIsPrimaryKey()) {
                        context.append(" [主键]");
                    }
                    if (field.getIsForeignKey() != null && field.getIsForeignKey()) {
                        context.append(" [外键]");
                    }
                    context.append("\n");
                }
                
                // 添加主键信息
                if (table.getPrimaryKey() != null) {
                    context.append("主键: ").append(table.getPrimaryKey()).append("\n");
                }
                
                // 添加外键关系
                if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                    context.append("外键关系:\n");
                    table.getForeignKeys().forEach((key, ref) -> 
                        context.append("  ").append(key).append(" -> ").append(ref).append("\n")
                    );
                }
            }
        }
        
        return context.toString();
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String dialect) {
        String dialectSpecifics = getDialectSpecifics(dialect);
        
        return String.format("""
            你是一个专业的SQL生成专家。你的任务是根据用户提供的自然语言查询和相关表字段信息，
            生成准确、高效的SQL语句。
            
            SQL方言: %s
            
            %s
            
            规则：
            1. 只使用提供的表和字段
            2. 正确使用JOIN连接相关表
            3. 使用适当的WHERE条件过滤数据
            4. 使用合适的聚合函数（COUNT, SUM, AVG等）当需要时
            5. 使用GROUP BY和HAVING当需要时
            6. 使用ORDER BY进行排序
            7. 使用LIMIT限制返回行数（默认100行）
            8. 使用表别名提高可读性
            9. 避免使用SELECT *，明确列出需要的字段
            10. 确保SQL语法正确，符合指定方言
            
            输出格式：
            {
              "sql": "生成的SQL语句",
              "explanation": "SQL语句的自然语言解释",
              "queryType": "SELECT/INSERT/UPDATE/DELETE",
              "tables": ["使用的表名"],
              "confidence": "high/medium/low"
            }
            """, dialect, dialectSpecifics);
    }
    
    /**
     * 获取方言特定的提示
     */
    private String getDialectSpecifics(String dialect) {
        return switch (dialect.toLowerCase()) {
            case "mysql" -> """
                MySQL特定规则：
                - 使用反引号(`)包裹表名和字段名
                - LIMIT子句格式: LIMIT offset, count 或 LIMIT count OFFSET offset
                - 支持REGEXP正则匹配
                - 日期函数: NOW(), DATE_FORMAT(), DATEDIFF()
                """;
            case "postgresql" -> """
                PostgreSQL特定规则：
                - 使用双引号(")包裹表名和字段名（如果需要）
                - LIMIT子句格式: LIMIT count OFFSET offset
                - 使用ILIKE进行不区分大小写的匹配
                - 日期函数: NOW(), TO_CHAR(), AGE()
                - 支持JSON/JSONB操作
                """;
            case "sqlite" -> """
                SQLite特定规则：
                - 使用双引号(")或方括号包裹标识符
                - LIMIT子句格式: LIMIT count OFFSET offset
                - 日期函数: datetime(), date(), strftime()
                - 不支持某些高级SQL特性
                """;
            case "oracle" -> """
                Oracle特定规则：
                - 使用双引号(")包裹标识符
                - 使用ROWNUM或FETCH FIRST限制行数
                - 日期函数: SYSDATE, TO_DATE(), TO_CHAR()
                - 使用DUAL表进行伪查询
                """;
            case "sqlserver" -> """
                SQL Server特定规则：
                - 使用方括号([])包裹标识符
                - 使用TOP限制行数: SELECT TOP n ...
                - 日期函数: GETDATE(), CONVERT(), FORMAT()
                - 使用OFFSET FETCH进行分页
                """;
            default -> """
                通用SQL规则：
                - 使用标准SQL语法
                - 使用LIMIT或TOP限制返回行数
                """;
        };
    }
    
    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(String naturalQuery, String context) {
        return String.format("""
            用户查询: %s
            
            相关表和字段信息:
            %s
            
            请根据以上信息生成SQL语句。
            只返回JSON格式的结果，不要包含其他解释文字。
            """, naturalQuery, context);
    }
    
    /**
     * 解析SQL生成响应
     */
    private SqlGenerationResult parseSqlResponse(String response) {
        try {
            String jsonStr = extractJson(response);
            if (jsonStr == null) {
                log.warn("No JSON found in SQL generator response");
                // 尝试直接提取SQL
                String sql = extractSqlDirectly(response);
                return new SqlGenerationResult(sql, "生成的SQL", "SELECT", List.of(), "medium");
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> result = mapper.readValue(jsonStr, Map.class);
            
            String sql = (String) result.get("sql");
            String explanation = (String) result.getOrDefault("explanation", "");
            String queryType = (String) result.getOrDefault("queryType", "SELECT");
            List<String> tables = (List<String>) result.getOrDefault("tables", List.of());
            String confidence = (String) result.getOrDefault("confidence", "medium");
            
            // 清理SQL语句
            sql = cleanSql(sql);
            
            log.info("SQL generated successfully: {}", sql);
            
            return new SqlGenerationResult(sql, explanation, queryType, tables, confidence);
            
        } catch (Exception e) {
            log.error("Failed to parse SQL generator response: {}", e.getMessage(), e);
            // 尝试直接提取SQL
            String sql = extractSqlDirectly(response);
            return new SqlGenerationResult(sql, "解析失败的SQL", "UNKNOWN", List.of(), "low");
        }
    }
    
    /**
     * 从响应中提取JSON
     */
    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return null;
    }
    
    /**
     * 直接提取SQL语句
     */
    private String extractSqlDirectly(String response) {
        // 尝试找到SQL代码块
        String sql = response;
        
        // 移除markdown代码块标记
        sql = sql.replaceAll("```sql", "");
        sql = sql.replaceAll("```", "");
        
        // 尝试提取SELECT语句
        int selectStart = sql.toUpperCase().indexOf("SELECT");
        int semicolonEnd = sql.indexOf(';', selectStart);
        
        if (selectStart != -1) {
            if (semicolonEnd != -1) {
                sql = sql.substring(selectStart, semicolonEnd + 1);
            } else {
                sql = sql.substring(selectStart);
            }
        }
        
        return cleanSql(sql);
    }
    
    /**
     * 清理SQL语句
     */
    private String cleanSql(String sql) {
        if (sql == null) return "";
        
        // 移除多余的空白
        sql = sql.trim();
        
        // 确保以分号结尾
        if (!sql.endsWith(";")) {
            sql = sql + ";";
        }
        
        return sql;
    }
    
    /**
     * SQL生成结果
     */
    public record SqlGenerationResult(
        String sql,
        String explanation,
        String queryType,
        List<String> tables,
        String confidence
    ) {}
}
