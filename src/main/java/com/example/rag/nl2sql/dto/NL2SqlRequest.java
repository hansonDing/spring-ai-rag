package com.example.rag.nl2sql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * NL2SQL 请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NL2SqlRequest {
    
    /**
     * 自然语言查询
     */
    private String query;
    
    /**
     * SQL方言类型（mysql, postgresql, sqlite等）
     */
    private String dialect;
    
    /**
     * 是否执行SQL（默认false，只生成不执行）
     */
    private Boolean execute;
    
    /**
     * 上下文信息（可选，如之前的问题）
     */
    private List<Map<String, String>> context;
    
    /**
     * 数据库名称（可选，用于限定查询范围）
     */
    private String databaseName;
    
    /**
     * 表名列表（可选，用于限定查询范围）
     */
    private List<String> tableNames;
    
    /**
     * 最大返回行数
     */
    private Integer maxRows;
    
    /**
     * 是否包含解释
     */
    private Boolean includeExplanation;
    
    /**
     * 获取方言，默认为mysql
     */
    public String getDialectOrDefault() {
        return dialect != null ? dialect : "mysql";
    }
    
    /**
     * 是否执行SQL
     */
    public boolean shouldExecute() {
        return execute != null && execute;
    }
    
    /**
     * 获取最大行数，默认100
     */
    public Integer getMaxRowsOrDefault() {
        return maxRows != null ? maxRows : 100;
    }
    
    /**
     * 是否包含解释
     */
    public boolean shouldIncludeExplanation() {
        return includeExplanation == null || includeExplanation;
    }
}
