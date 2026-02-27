package com.example.rag.nl2sql.config;

import com.example.rag.nl2sql.entity.ColumnInfo;
import com.example.rag.nl2sql.entity.TableInfo;
import com.example.rag.nl2sql.service.SchemaEmbeddingService;
import com.example.rag.nl2sql.service.TableSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * NL2SQL 初始化配置
 * 应用启动时自动加载示例表结构数据
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NL2SQLDataInitializer {
    
    private final TableSchemaService tableSchemaService;
    private final SchemaEmbeddingService schemaEmbeddingService;
    
    @Bean
    @Order(2)
    public CommandLineRunner initSampleData() {
        return args -> {
            // 检查是否已有数据
            if (tableSchemaService.getTableCount() > 0) {
                log.info("Table schema already initialized, skipping...");
                return;
            }
            
            log.info("Initializing NL2SQL sample table schemas...");
            
            // 初始化用户表
            initUsersTable();
            
            // 初始化分类表
            initCategoriesTable();
            
            // 初始化商品表
            initProductsTable();
            
            // 初始化订单表
            initOrdersTable();
            
            // 初始化订单商品表
            initOrderItemsTable();
            
            log.info("NL2SQL sample data initialization completed. Total tables: {}", tableSchemaService.getTableCount());
        };
    }
    
    private void initUsersTable() {
        TableInfo users = TableInfo.builder()
                .tableName("users")
                .tableAlias("用户表")
                .description("存储注册用户的基本信息，包括用户名、邮箱、手机号、状态等。用于用户认证、用户信息查询和用户行为分析。")
                .dbType("mysql")
                .databaseName("ecommerce")
                .primaryKey("user_id")
                .columns(Arrays.asList(
                    ColumnInfo.builder().columnName("user_id").columnAlias("用户ID").dataType("BIGINT").isPrimaryKey(true).isAutoIncrement(true).description("用户唯一标识").build(),
                    ColumnInfo.builder().columnName("username").columnAlias("用户名").dataType("VARCHAR").dataLength(50).nullable(false).description("用户登录名").build(),
                    ColumnInfo.builder().columnName("email").columnAlias("邮箱").dataType("VARCHAR").dataLength(100).nullable(false).description("用户邮箱地址").build(),
                    ColumnInfo.builder().columnName("phone").columnAlias("手机号").dataType("VARCHAR").dataLength(20).description("用户手机号").build(),
                    ColumnInfo.builder().columnName("status").columnAlias("状态").dataType("TINYINT").defaultValue("1").description("用户状态：0-禁用，1-正常").build(),
                    ColumnInfo.builder().columnName("created_at").columnAlias("创建时间").dataType("DATETIME").description("用户注册时间").build(),
                    ColumnInfo.builder().columnName("updated_at").columnAlias("更新时间").dataType("DATETIME").description("最后更新时间").build(),
                    ColumnInfo.builder().columnName("last_login_at").columnAlias("最后登录时间").dataType("DATETIME").description("最后登录时间").build(),
                    ColumnInfo.builder().columnName("gender").columnAlias("性别").dataType("TINYINT").description("性别：0-未知，1-男，2-女").build(),
                    ColumnInfo.builder().columnName("birth_date").columnAlias("生日").dataType("DATE").description("用户生日").build(),
                    ColumnInfo.builder().columnName("city").columnAlias("城市").dataType("VARCHAR").dataLength(50).description("所在城市").build()
                ))
                .build();
        
        tableSchemaService.saveTable(convertToRequest(users));
        schemaEmbeddingService.addTableEmbedding(users);
        log.info("Initialized users table");
    }
    
    private void initCategoriesTable() {
        TableInfo categories = TableInfo.builder()
                .tableName("categories")
                .tableAlias("商品分类表")
                .description("存储商品分类信息，支持多级分类结构。包括分类名称、层级、排序等信息。用于商品分类管理和商品浏览导航。")
                .dbType("mysql")
                .databaseName("ecommerce")
                .primaryKey("category_id")
                .columns(Arrays.asList(
                    ColumnInfo.builder().columnName("category_id").columnAlias("分类ID").dataType("INT").isPrimaryKey(true).isAutoIncrement(true).description("分类唯一标识").build(),
                    ColumnInfo.builder().columnName("category_name").columnAlias("分类名称").dataType("VARCHAR").dataLength(50).nullable(false).description("分类名称").build(),
                    ColumnInfo.builder().columnName("parent_id").columnAlias("父分类ID").dataType("INT").defaultValue("0").description("父分类ID，0表示顶级分类").build(),
                    ColumnInfo.builder().columnName("level").columnAlias("层级").dataType("TINYINT").defaultValue("1").description("分类层级").build(),
                    ColumnInfo.builder().columnName("sort_order").columnAlias("排序").dataType("INT").defaultValue("0").description("排序顺序").build(),
                    ColumnInfo.builder().columnName("status").columnAlias("状态").dataType("TINYINT").defaultValue("1").description("状态：0-禁用，1-启用").build(),
                    ColumnInfo.builder().columnName("created_at").columnAlias("创建时间").dataType("DATETIME").description("创建时间").build(),
                    ColumnInfo.builder().columnName("description").columnAlias("描述").dataType("VARCHAR").dataLength(500).description("分类描述").build()
                ))
                .build();
        
        tableSchemaService.saveTable(convertToRequest(categories));
        schemaEmbeddingService.addTableEmbedding(categories);
        log.info("Initialized categories table");
    }
    
    private void initProductsTable() {
        TableInfo products = TableInfo.builder()
                .tableName("products")
                .tableAlias("商品表")
                .description("存储商品信息，包括商品名称、价格、库存、状态等。与categories表关联。用于商品展示、搜索、库存管理等。")
                .dbType("mysql")
                .databaseName("ecommerce")
                .primaryKey("product_id")
                .columns(Arrays.asList(
                    ColumnInfo.builder().columnName("product_id").columnAlias("商品ID").dataType("BIGINT").isPrimaryKey(true).isAutoIncrement(true).description("商品唯一标识").build(),
                    ColumnInfo.builder().columnName("product_name").columnAlias("商品名称").dataType("VARCHAR").dataLength(200).nullable(false).description("商品名称").build(),
                    ColumnInfo.builder().columnName("category_id").columnAlias("分类ID").dataType("INT").nullable(false).isForeignKey(true).foreignKeyTable("categories").foreignKeyColumn("category_id").description("所属分类ID").build(),
                    ColumnInfo.builder().columnName("brand").columnAlias("品牌").dataType("VARCHAR").dataLength(50).description("品牌名称").build(),
                    ColumnInfo.builder().columnName("description").columnAlias("描述").dataType("TEXT").description("商品详细描述").build(),
                    ColumnInfo.builder().columnName("price").columnAlias("售价").dataType("DECIMAL").dataLength(10).decimalScale(2).nullable(false).description("商品售价").build(),
                    ColumnInfo.builder().columnName("original_price").columnAlias("原价").dataType("DECIMAL").dataLength(10).decimalScale(2).description("商品原价").build(),
                    ColumnInfo.builder().columnName("stock_quantity").columnAlias("库存").dataType("INT").defaultValue("0").description("库存数量").build(),
                    ColumnInfo.builder().columnName("sold_quantity").columnAlias("销量").dataType("INT").defaultValue("0").description("已售数量").build(),
                    ColumnInfo.builder().columnName("status").columnAlias("状态").dataType("TINYINT").defaultValue("1").description("状态：0-下架，1-上架，2-缺货").build(),
                    ColumnInfo.builder().columnName("created_at").columnAlias("创建时间").dataType("DATETIME").description("创建时间").build(),
                    ColumnInfo.builder().columnName("updated_at").columnAlias("更新时间").dataType("DATETIME").description("更新时间").build(),
                    ColumnInfo.builder().columnName("is_hot").columnAlias("热销").dataType("TINYINT").defaultValue("0").description("是否热销：0-否，1-是").build(),
                    ColumnInfo.builder().columnName("is_new").columnAlias("新品").dataType("TINYINT").defaultValue("0").description("是否新品：0-否，1-是").build(),
                    ColumnInfo.builder().columnName("rating").columnAlias("评分").dataType("DECIMAL").dataLength(2).decimalScale(1).defaultValue("5.0").description("商品评分（1-5）").build(),
                    ColumnInfo.builder().columnName("review_count").columnAlias("评价数").dataType("INT").defaultValue("0").description("评价数量").build()
                ))
                .build();
        
        tableSchemaService.saveTable(convertToRequest(products));
        schemaEmbeddingService.addTableEmbedding(products);
        log.info("Initialized products table");
    }
    
    private void initOrdersTable() {
        TableInfo orders = TableInfo.builder()
                .tableName("orders")
                .tableAlias("订单表")
                .description("存储用户订单信息，包括订单状态、金额、收货信息等。与users表关联。用于订单管理、销售统计、物流跟踪等。")
                .dbType("mysql")
                .databaseName("ecommerce")
                .primaryKey("order_id")
                .columns(Arrays.asList(
                    ColumnInfo.builder().columnName("order_id").columnAlias("订单ID").dataType("BIGINT").isPrimaryKey(true).isAutoIncrement(true).description("订单唯一标识").build(),
                    ColumnInfo.builder().columnName("order_no").columnAlias("订单编号").dataType("VARCHAR").dataLength(32).nullable(false).description("订单编号").build(),
                    ColumnInfo.builder().columnName("user_id").columnAlias("用户ID").dataType("BIGINT").nullable(false).isForeignKey(true).foreignKeyTable("users").foreignKeyColumn("user_id").description("下单用户ID").build(),
                    ColumnInfo.builder().columnName("order_status").columnAlias("订单状态").dataType("TINYINT").defaultValue("0").description("状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消").build(),
                    ColumnInfo.builder().columnName("total_amount").columnAlias("订单总额").dataType("DECIMAL").dataLength(12).decimalScale(2).nullable(false).description("订单总金额").build(),
                    ColumnInfo.builder().columnName("discount_amount").columnAlias("优惠金额").dataType("DECIMAL").dataLength(12).decimalScale(2).defaultValue("0").description("优惠金额").build(),
                    ColumnInfo.builder().columnName("shipping_fee").columnAlias("运费").dataType("DECIMAL").dataLength(10).decimalScale(2).defaultValue("0").description("运费").build(),
                    ColumnInfo.builder().columnName("pay_amount").columnAlias("实付金额").dataType("DECIMAL").dataLength(12).decimalScale(2).nullable(false).description("实付金额").build(),
                    ColumnInfo.builder().columnName("pay_time").columnAlias("支付时间").dataType("DATETIME").description("支付时间").build(),
                    ColumnInfo.builder().columnName("ship_time").columnAlias("发货时间").dataType("DATETIME").description("发货时间").build(),
                    ColumnInfo.builder().columnName("receive_time").columnAlias("收货时间").dataType("DATETIME").description("收货时间").build(),
                    ColumnInfo.builder().columnName("created_at").columnAlias("创建时间").dataType("DATETIME").description("订单创建时间").build(),
                    ColumnInfo.builder().columnName("updated_at").columnAlias("更新时间").dataType("DATETIME").description("更新时间").build(),
                    ColumnInfo.builder().columnName("shipping_address").columnAlias("收货地址").dataType("VARCHAR").dataLength(500).description("收货地址").build(),
                    ColumnInfo.builder().columnName("receiver_name").columnAlias("收货人").dataType("VARCHAR").dataLength(50).description("收货人姓名").build(),
                    ColumnInfo.builder().columnName("receiver_phone").columnAlias("收货电话").dataType("VARCHAR").dataLength(20).description("收货人电话").build(),
                    ColumnInfo.builder().columnName("pay_method").columnAlias("支付方式").dataType("TINYINT").description("支付方式：1-支付宝，2-微信，3-银行卡").build()
                ))
                .build();
        
        tableSchemaService.saveTable(convertToRequest(orders));
        schemaEmbeddingService.addTableEmbedding(orders);
        log.info("Initialized orders table");
    }
    
    private void initOrderItemsTable() {
        TableInfo orderItems = TableInfo.builder()
                .tableName("order_items")
                .tableAlias("订单商品表")
                .description("存储订单中的商品明细，包括商品快照信息、价格、数量等。与orders和products表关联。用于订单详情展示、销售统计。")
                .dbType("mysql")
                .databaseName("ecommerce")
                .primaryKey("item_id")
                .columns(Arrays.asList(
                    ColumnInfo.builder().columnName("item_id").columnAlias("订单商品ID").dataType("BIGINT").isPrimaryKey(true).isAutoIncrement(true).description("订单商品唯一标识").build(),
                    ColumnInfo.builder().columnName("order_id").columnAlias("订单ID").dataType("BIGINT").nullable(false).isForeignKey(true).foreignKeyTable("orders").foreignKeyColumn("order_id").description("所属订单ID").build(),
                    ColumnInfo.builder().columnName("product_id").columnAlias("商品ID").dataType("BIGINT").nullable(false).isForeignKey(true).foreignKeyTable("products").foreignKeyColumn("product_id").description("商品ID").build(),
                    ColumnInfo.builder().columnName("product_name").columnAlias("商品名称").dataType("VARCHAR").dataLength(200).nullable(false).description("商品名称（快照）").build(),
                    ColumnInfo.builder().columnName("product_image").columnAlias("商品图片").dataType("VARCHAR").dataLength(255).description("商品图片（快照）").build(),
                    ColumnInfo.builder().columnName("price").columnAlias("单价").dataType("DECIMAL").dataLength(10).decimalScale(2).nullable(false).description("商品单价").build(),
                    ColumnInfo.builder().columnName("quantity").columnAlias("数量").dataType("INT").nullable(false).description("购买数量").build(),
                    ColumnInfo.builder().columnName("subtotal").columnAlias("小计").dataType("DECIMAL").dataLength(12).decimalScale(2).nullable(false).description("小计金额").build(),
                    ColumnInfo.builder().columnName("created_at").columnAlias("创建时间").dataType("DATETIME").description("创建时间").build()
                ))
                .build();
        
        tableSchemaService.saveTable(convertToRequest(orderItems));
        schemaEmbeddingService.addTableEmbedding(orderItems);
        log.info("Initialized order_items table");
    }
    
    /**
     * 将TableInfo转换为TableInfoRequest
     */
    private com.example.rag.nl2sql.dto.TableInfoRequest convertToRequest(TableInfo tableInfo) {
        List<com.example.rag.nl2sql.dto.TableInfoRequest.ColumnInfoRequest> columnRequests = tableInfo.getColumns().stream()
                .map(col -> com.example.rag.nl2sql.dto.TableInfoRequest.ColumnInfoRequest.builder()
                        .columnName(col.getColumnName())
                        .columnAlias(col.getColumnAlias())
                        .description(col.getDescription())
                        .dataType(col.getDataType())
                        .dataLength(col.getDataLength())
                        .decimalScale(col.getDecimalScale())
                        .nullable(col.getNullable())
                        .defaultValue(col.getDefaultValue())
                        .isPrimaryKey(col.getIsPrimaryKey())
                        .isForeignKey(col.getIsForeignKey())
                        .foreignKeyTable(col.getForeignKeyTable())
                        .foreignKeyColumn(col.getForeignKeyColumn())
                        .isAutoIncrement(col.getIsAutoIncrement())
                        .ordinalPosition(col.getOrdinalPosition())
                        .build())
                .toList();
        
        return com.example.rag.nl2sql.dto.TableInfoRequest.builder()
                .tableName(tableInfo.getTableName())
                .tableAlias(tableInfo.getTableAlias())
                .description(tableInfo.getDescription())
                .dbType(tableInfo.getDbType())
                .databaseName(tableInfo.getDatabaseName())
                .primaryKey(tableInfo.getPrimaryKey())
                .columns(columnRequests)
                .build();
    }
}
