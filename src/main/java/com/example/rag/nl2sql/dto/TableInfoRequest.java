package com.example.rag.nl2sql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 表信息请求/响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfoRequest {
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表中文名称/别名
     */
    private String tableAlias;
    
    /**
     * 表描述
     */
    private String description;
    
    /**
     * 数据库类型
     */
    private String dbType;
    
    /**
     * 数据库名称
     */
    private String databaseName;
    
    /**
     * schema名称
     */
    private String schemaName;
    
    /**
     * 字段列表
     */
    private List<ColumnInfoRequest> columns;
    
    /**
     * 主键字段名
     */
    private String primaryKey;
    
    /**
     * 外键关系
     */
    private Map<String, String> foreignKeys;
    
    /**
     * 索引信息
     */
    private List<Map<String, Object>> indexes;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 字段信息请求DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnInfoRequest {
        private String columnName;
        private String columnAlias;
        private String description;
        private String dataType;
        private Integer dataLength;
        private Integer decimalScale;
        private Boolean nullable;
        private String defaultValue;
        private Boolean isPrimaryKey;
        private Boolean isForeignKey;
        private String foreignKeyTable;
        private String foreignKeyColumn;
        private Boolean isAutoIncrement;
        private Integer ordinalPosition;
    }
}
