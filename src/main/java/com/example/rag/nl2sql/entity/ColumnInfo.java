package com.example.rag.nl2sql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 字段信息实体类
 * 存储表中每个字段的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo {
    
    /**
     * 字段ID
     */
    private String id;
    
    /**
     * 所属表ID
     */
    private String tableId;
    
    /**
     * 字段名
     */
    private String columnName;
    
    /**
     * 字段中文名称/别名
     */
    private String columnAlias;
    
    /**
     * 字段描述
     */
    private String description;
    
    /**
     * 数据类型（VARCHAR, INT, DECIMAL等）
     */
    private String dataType;
    
    /**
     * 数据类型长度（如VARCHAR(255)中的255）
     */
    private Integer dataLength;
    
    /**
     * 小数位数（DECIMAL类型用）
     */
    private Integer decimalScale;
    
    /**
     * 是否可为空
     */
    private Boolean nullable;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 是否为主键
     */
    private Boolean isPrimaryKey;
    
    /**
     * 是否为外键
     */
    private Boolean isForeignKey;
    
    /**
     * 外键引用的表
     */
    private String foreignKeyTable;
    
    /**
     * 外键引用的字段
     */
    private String foreignKeyColumn;
    
    /**
     * 是否自增
     */
    private Boolean isAutoIncrement;
    
    /**
     * 字段顺序
     */
    private Integer ordinalPosition;
    
    /**
     * 字符集
     */
    private String characterSet;
    
    /**
     * 排序规则
     */
    private String collation;
    
    /**
     * 额外元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 获取字段的完整描述文本
     */
    public String toDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(columnName);
        if (columnAlias != null && !columnAlias.isEmpty()) {
            desc.append("(").append(columnAlias).append(")");
        }
        desc.append(" ").append(dataType);
        if (dataLength != null) {
            desc.append("(").append(dataLength);
            if (decimalScale != null) {
                desc.append(",").append(decimalScale);
            }
            desc.append(")");
        }
        if (description != null && !description.isEmpty()) {
            desc.append(" - ").append(description);
        }
        if (isPrimaryKey != null && isPrimaryKey) {
            desc.append(" [PK]");
        }
        if (isForeignKey != null && isForeignKey) {
            desc.append(" [FK]");
        }
        return desc.toString();
    }
    
    /**
     * 转换为JSON对象格式
     */
    public Map<String, Object> toMap() {
        return Map.of(
            "name", columnName,
            "alias", columnAlias != null ? columnAlias : "",
            "type", dataType,
            "description", description != null ? description : "",
            "nullable", nullable != null ? nullable : true,
            "isPrimaryKey", isPrimaryKey != null ? isPrimaryKey : false
        );
    }
}
