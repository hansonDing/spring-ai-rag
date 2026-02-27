package com.example.rag.nl2sql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * NL2SQL 响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NL2SqlResponse {
    
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
     * SQL方言类型
     */
    private String sqlDialect;
    
    /**
     * 查询执行状态
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
     * 总处理时间（毫秒）
     */
    private Long totalProcessingTimeMs;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 使用的表信息
     */
    private List<TableInfoDTO> usedTables;
    
    /**
     * 查询解释
     */
    private String explanation;
    
    /**
     * 是否为只读查询
     */
    private Boolean isReadOnly;
    
    /**
     * 查询类型
     */
    private String queryType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 表信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableInfoDTO {
        private String tableName;
        private String tableAlias;
        private String description;
        private List<String> columns;
    }
    
    /**
     * 创建成功的响应
     */
    public static NL2SqlResponse success(String id, String naturalQuery, String sql, String dialect,
                                          List<Map<String, Object>> data, List<String> columns,
                                          Long executionTimeMs, Long totalProcessingTimeMs) {
        return NL2SqlResponse.builder()
                .id(id)
                .naturalLanguageQuery(naturalQuery)
                .generatedSql(sql)
                .sqlDialect(dialect)
                .status("success")
                .data(data)
                .columns(columns)
                .rowCount(data != null ? data.size() : 0)
                .executionTimeMs(executionTimeMs)
                .totalProcessingTimeMs(totalProcessingTimeMs)
                .createTime(LocalDateTime.now())
                .isReadOnly(sql != null && sql.trim().toUpperCase().startsWith("SELECT"))
                .queryType(detectQueryType(sql))
                .build();
    }
    
    /**
     * 创建失败的响应
     */
    public static NL2SqlResponse error(String id, String naturalQuery, String sql, String dialect,
                                        String errorMessage, Long totalProcessingTimeMs) {
        return NL2SqlResponse.builder()
                .id(id)
                .naturalLanguageQuery(naturalQuery)
                .generatedSql(sql)
                .sqlDialect(dialect)
                .status("error")
                .errorMessage(errorMessage)
                .totalProcessingTimeMs(totalProcessingTimeMs)
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建仅生成SQL的响应
     */
    public static NL2SqlResponse generated(String id, String naturalQuery, String sql, String dialect,
                                            Long totalProcessingTimeMs) {
        return NL2SqlResponse.builder()
                .id(id)
                .naturalLanguageQuery(naturalQuery)
                .generatedSql(sql)
                .sqlDialect(dialect)
                .status("generated")
                .totalProcessingTimeMs(totalProcessingTimeMs)
                .createTime(LocalDateTime.now())
                .isReadOnly(sql != null && sql.trim().toUpperCase().startsWith("SELECT"))
                .queryType(detectQueryType(sql))
                .build();
    }
    
    private static String detectQueryType(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "UNKNOWN";
        }
        String upperSql = sql.trim().toUpperCase();
        if (upperSql.startsWith("SELECT")) return "SELECT";
        if (upperSql.startsWith("INSERT")) return "INSERT";
        if (upperSql.startsWith("UPDATE")) return "UPDATE";
        if (upperSql.startsWith("DELETE")) return "DELETE";
        return "OTHER";
    }
}
