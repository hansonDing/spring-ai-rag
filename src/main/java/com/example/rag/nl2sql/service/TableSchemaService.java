package com.example.rag.nl2sql.service;

import com.example.rag.nl2sql.dto.TableInfoRequest;
import com.example.rag.nl2sql.entity.ColumnInfo;
import com.example.rag.nl2sql.entity.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 表结构管理服务
 * 管理数据库表信息的增删改查
 */
@Slf4j
@Service
public class TableSchemaService {
    
    // 内存存储：表ID -> 表信息
    private final Map<String, TableInfo> tableStore = new ConcurrentHashMap<>();
    
    // 内存存储：表名 -> 表ID
    private final Map<String, String> tableNameIndex = new ConcurrentHashMap<>();
    
    /**
     * 创建或更新表信息
     */
    public TableInfo saveTable(TableInfoRequest request) {
        String tableId = tableNameIndex.get(request.getTableName());
        
        if (tableId != null) {
            // 更新现有表
            return updateTable(tableId, request);
        } else {
            // 创建新表
            return createTable(request);
        }
    }
    
    /**
     * 创建新表
     */
    private TableInfo createTable(TableInfoRequest request) {
        String id = UUID.randomUUID().toString();
        
        TableInfo tableInfo = TableInfo.builder()
                .id(id)
                .tableName(request.getTableName())
                .tableAlias(request.getTableAlias())
                .description(request.getDescription())
                .dbType(request.getDbType())
                .databaseName(request.getDatabaseName())
                .schemaName(request.getSchemaName())
                .columns(convertColumns(request.getColumns(), id))
                .primaryKey(request.getPrimaryKey())
                .foreignKeys(request.getForeignKeys())
                .indexes(request.getIndexes())
                .metadata(request.getMetadata())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .enabled(true)
                .build();
        
        tableStore.put(id, tableInfo);
        tableNameIndex.put(request.getTableName(), id);
        
        log.info("Table created: {} ({})", request.getTableName(), id);
        return tableInfo;
    }
    
    /**
     * 更新表信息
     */
    private TableInfo updateTable(String id, TableInfoRequest request) {
        TableInfo existing = tableStore.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Table not found: " + id);
        }
        
        // 如果表名变更，更新索引
        if (!existing.getTableName().equals(request.getTableName())) {
            tableNameIndex.remove(existing.getTableName());
            tableNameIndex.put(request.getTableName(), id);
        }
        
        TableInfo updated = TableInfo.builder()
                .id(id)
                .tableName(request.getTableName())
                .tableAlias(request.getTableAlias())
                .description(request.getDescription())
                .dbType(request.getDbType())
                .databaseName(request.getDatabaseName())
                .schemaName(request.getSchemaName())
                .columns(convertColumns(request.getColumns(), id))
                .primaryKey(request.getPrimaryKey())
                .foreignKeys(request.getForeignKeys())
                .indexes(request.getIndexes())
                .metadata(request.getMetadata())
                .createTime(existing.getCreateTime())
                .updateTime(LocalDateTime.now())
                .enabled(existing.getEnabled())
                .build();
        
        tableStore.put(id, updated);
        log.info("Table updated: {} ({})", request.getTableName(), id);
        return updated;
    }
    
    /**
     * 转换字段请求为字段实体
     */
    private List<ColumnInfo> convertColumns(List<TableInfoRequest.ColumnInfoRequest> columnRequests, String tableId) {
        if (columnRequests == null) {
            return new ArrayList<>();
        }
        
        return columnRequests.stream()
                .map(colReq -> ColumnInfo.builder()
                        .id(UUID.randomUUID().toString())
                        .tableId(tableId)
                        .columnName(colReq.getColumnName())
                        .columnAlias(colReq.getColumnAlias())
                        .description(colReq.getDescription())
                        .dataType(colReq.getDataType())
                        .dataLength(colReq.getDataLength())
                        .decimalScale(colReq.getDecimalScale())
                        .nullable(colReq.getNullable())
                        .defaultValue(colReq.getDefaultValue())
                        .isPrimaryKey(colReq.getIsPrimaryKey())
                        .isForeignKey(colReq.getIsForeignKey())
                        .foreignKeyTable(colReq.getForeignKeyTable())
                        .foreignKeyColumn(colReq.getForeignKeyColumn())
                        .isAutoIncrement(colReq.getIsAutoIncrement())
                        .ordinalPosition(colReq.getOrdinalPosition())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取表信息
     */
    public TableInfo getTableById(String id) {
        return tableStore.get(id);
    }
    
    /**
     * 根据表名获取表信息
     */
    public TableInfo getTableByName(String tableName) {
        String id = tableNameIndex.get(tableName);
        return id != null ? tableStore.get(id) : null;
    }
    
    /**
     * 获取所有表信息
     */
    public List<TableInfo> getAllTables() {
        return new ArrayList<>(tableStore.values());
    }
    
    /**
     * 根据数据库名称获取表列表
     */
    public List<TableInfo> getTablesByDatabase(String databaseName) {
        return tableStore.values().stream()
                .filter(t -> databaseName.equals(t.getDatabaseName()))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据数据库类型获取表列表
     */
    public List<TableInfo> getTablesByDbType(String dbType) {
        return tableStore.values().stream()
                .filter(t -> dbType.equalsIgnoreCase(t.getDbType()))
                .collect(Collectors.toList());
    }
    
    /**
     * 删除表信息
     */
    public void deleteTable(String id) {
        TableInfo table = tableStore.remove(id);
        if (table != null) {
            tableNameIndex.remove(table.getTableName());
            log.info("Table deleted: {} ({})", table.getTableName(), id);
        }
    }
    
    /**
     * 根据表名删除表信息
     */
    public void deleteTableByName(String tableName) {
        String id = tableNameIndex.remove(tableName);
        if (id != null) {
            TableInfo table = tableStore.remove(id);
            log.info("Table deleted: {} ({})", tableName, id);
        }
    }
    
    /**
     * 批量保存表信息
     */
    public List<TableInfo> saveTables(List<TableInfoRequest> requests) {
        return requests.stream()
                .map(this::saveTable)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查表是否存在
     */
    public boolean exists(String tableName) {
        return tableNameIndex.containsKey(tableName);
    }
    
    /**
     * 获取表总数
     */
    public int getTableCount() {
        return tableStore.size();
    }
    
    /**
     * 根据表ID列表获取表信息
     */
    public List<TableInfo> getTablesByIds(List<String> ids) {
        return ids.stream()
                .map(tableStore::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据表名列表获取表信息
     */
    public List<TableInfo> getTablesByNames(List<String> tableNames) {
        return tableNames.stream()
                .map(this::getTableByName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
