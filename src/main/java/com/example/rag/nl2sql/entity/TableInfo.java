package com.example.rag.nl2sql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 表信息实体类
 * 存储数据库表的结构信息，包括表描述（用于向量化检索）和字段信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo {
    
    /**
     * 表ID（唯一标识）
     */
    private String id;
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表中文名称/别名
     */
    private String tableAlias;
    
    /**
     * 表描述（用于向量化检索）
     */
    private String description;
    
    /**
     * 数据库类型（mysql, postgresql, sqlite等）
     */
    private String dbType;
    
    /**
     * 数据库名称
     */
    private String databaseName;
    
    /**
     * 表所属schema
     */
    private String schemaName;
    
    /**
     * 字段列表（以JSON字符串形式存储，不用于向量化）
     */
    private List<ColumnInfo> columns;
    
    /**
     * 主键字段名
     */
    private String primaryKey;
    
    /**
     * 外键关系（JSON格式）
     */
    private Map<String, String> foreignKeys;
    
    /**
     * 索引信息（JSON格式）
     */
    private List<Map<String, Object>> indexes;
    
    /**
     * 表行数估计
     */
    private Long estimatedRows;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 获取用于向量化的文本内容
     * 组合表名、别名和描述，用于语义检索
     */
    public String toEmbeddingText() {
        StringBuilder sb = new StringBuilder();
        sb.append("表名: ").append(tableName);
        if (tableAlias != null && !tableAlias.isEmpty()) {
            sb.append(", 别名: ").append(tableAlias);
        }
        sb.append("\n描述: ").append(description);
        if (columns != null && !columns.isEmpty()) {
            sb.append("\n包含字段: ");
            columns.stream()
                    .limit(5) // 只取前5个字段用于描述
                    .forEach(col -> sb.append(col.getColumnName()).append(", "));
            if (columns.size() > 5) {
                sb.append("...等共").append(columns.size()).append("个字段");
            }
        }
        return sb.toString();
    }
    
    /**
     * 获取表的完整DDL描述
     */
    public String toDDL() {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                ColumnInfo col = columns.get(i);
                ddl.append("  ").append(col.getColumnName())
                   .append(" ").append(col.getDataType());
                if (col.getNullable() != null && !col.getNullable()) {
                    ddl.append(" NOT NULL");
                }
                if (i < columns.size() - 1) {
                    ddl.append(",");
                }
                ddl.append("\n");
            }
        }
        
        ddl.append(");");
        return ddl.toString();
    }
}
