package com.example.rag.nl2sql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * SQL查询结果实体类
 * 存储NL2SQL生成的SQL语句及其执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlQueryResult {
    
    /**
     * 查询ID
     */
    private String id;
    
    /**
     * 原始自然语言问题
     */
    private String naturalLanguageQuery;
    
    /**
     * 生成的SQL语句
     */
    private String generatedSql;
    
    /**
     * SQL方言类型（mysql, postgresql, sqlite等）
     */
    private String sqlDialect;
    
    /**
     * 查询执行状态（success, error, pending）
     */
    private String status;
    
    /**
     * 查询结果数据
     */
    private List<Map<String, Object>> data;
    
    /**
     * 结果列名
     */
    private List<String> columns;
    
    /**
     * 结果行数
     */
    private Integer rowCount;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTimeMs;
    
    /**
     * 错误信息（如果执行失败）
     */
    private String errorMessage;
    
    /**
     * 使用的表信息
     */
    private List<TableInfo> usedTables;
    
    /**
     * 提取的相关字段
     */
    private List<ColumnInfo> extractedFields;
    
    /**
     * 置信度分数（0-1）
     */
    private Double confidenceScore;
    
    /**
     * 查询解释（自然语言描述SQL做什么）
     */
    private String explanation;
    
    /**
     * 查询创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 查询执行时间
     */
    private LocalDateTime executeTime;
    
    /**
     * 是否为只读查询（SELECT）
     */
    private Boolean isReadOnly;
    
    /**
     * 查询类型（SELECT, INSERT, UPDATE, DELETE等）
     */
    private String queryType;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建成功的查询结果
     */
    public static SqlQueryResult success(String naturalQuery, String sql, List<Map<String, Object>> data, 
                                          List<String> columns, Long executionTimeMs) {
        return SqlQueryResult.builder()
                .id(java.util.UUID.randomUUID().toString())
                .naturalLanguageQuery(naturalQuery)
                .generatedSql(sql)
                .status("success")
                .data(data)
                .columns(columns)
                .rowCount(data != null ? data.size() : 0)
                .executionTimeMs(executionTimeMs)
                .createTime(LocalDateTime.now())
                .executeTime(LocalDateTime.now())
                .isReadOnly(sql != null && sql.trim().toUpperCase().startsWith("SELECT"))
                .queryType(detectQueryType(sql))
                .build();
    }
    
    /**
     * 创建失败的查询结果
     */
    public static SqlQueryResult error(String naturalQuery, String sql, String errorMessage) {
        return SqlQueryResult.builder()
                .id(java.util.UUID.randomUUID().toString())
                .naturalLanguageQuery(naturalQuery)
                .generatedSql(sql)
                .status("error")
                .errorMessage(errorMessage)
                .createTime(LocalDateTime.now())
                .executeTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建待执行的查询结果
     */
    public static SqlQueryResult pending(String naturalQuery, String sql) {
        return SqlQueryResult.builder()
                .id(java.util.UUID.randomUUID().toString())
                .naturalLanguageQuery(naturalQuery)
                .generatedSql(sql)
                .status("pending")
                .createTime(LocalDateTime.now())
                .isReadOnly(sql != null && sql.trim().toUpperCase().startsWith("SELECT"))
                .queryType(detectQueryType(sql))
                .build();
    }
    
    /**
     * 检测查询类型
     */
    private static String detectQueryType(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "UNKNOWN";
        }
        String upperSql = sql.trim().toUpperCase();
        if (upperSql.startsWith("SELECT")) return "SELECT";
        if (upperSql.startsWith("INSERT")) return "INSERT";
        if (upperSql.startsWith("UPDATE")) return "UPDATE";
        if (upperSql.startsWith("DELETE")) return "DELETE";
        if (upperSql.startsWith("CREATE")) return "CREATE";
        if (upperSql.startsWith("ALTER")) return "ALTER";
        if (upperSql.startsWith("DROP")) return "DROP";
        return "OTHER";
    }
}
