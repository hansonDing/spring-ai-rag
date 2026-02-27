# Spring AI NL2SQL

基于 **Spring Boot 3.2** + **Spring AI 0.8** + **Elasticsearch 8** + **DeepSeek** 的 NL2SQL（自然语言生成 SQL）系统。

## 系统架构

```
用户提问 → 向量检索表描述 → Agent 1 提取字段 → Agent 2 生成 SQL
```

### 核心组件

1. **向量存储层**: 表描述信息向量化存储到 Elasticsearch，用于语义检索
2. **文本存储层**: 表字段信息以 JSON 形式存储，供 Agent 使用
3. **Agent 1 - 字段提取专家**: 从大量字段信息中提取相关字段
4. **Agent 2 - SQL 生成专家**: 根据提取的字段生成精准 SQL

## 技术栈

- Spring Boot 3.2.x
- Spring AI 0.8.x
- Elasticsearch 8.17.6 (向量数据库)
- DeepSeek (大语言模型)
- Java 17+

## 项目结构

```
spring-ai-rag/
├── src/main/java/com/example/rag/
│   ├── SpringAiRagApplication.java
│   ├── config/              # 配置类
│   │   ├── ElasticsearchConfig.java
│   │   ├── AiConfig.java
│   │   └── NL2SQLDataInitializer.java  # 示例数据初始化
│   ├── controller/          # REST API
│   │   ├── RagController.java
│   │   └── NL2SQLController.java
│   ├── service/             # 业务逻辑
│   │   ├── RagService.java
│   │   ├── EmbeddingService.java
│   │   ├── TableSchemaService.java     # 表结构管理
│   │   ├── SchemaEmbeddingService.java # 表描述向量化
│   │   ├── FieldExtractorAgent.java    # Agent 1: 字段提取
│   │   ├── SqlGeneratorAgent.java      # Agent 2: SQL 生成
│   │   └── NL2SQLService.java          # NL2SQL 主服务
│   ├── entity/              # 实体类
│   │   ├── Document.java
│   │   ├── TableInfo.java              # 表信息
│   │   ├── ColumnInfo.java             # 字段信息
│   │   └── SqlQueryResult.java         # 查询结果
│   └── repository/          # 数据访问
├── src/main/resources/
│   ├── application.yml
│   └── prompts/
│       ├── rag-system-prompt.st
│       ├── field-extractor-prompt.st   # 字段提取 Agent 提示词
│       └── sql-generator-prompt.st     # SQL 生成 Agent 提示词
└── pom.xml
```

## 快速开始

### 1. 启动 Elasticsearch

```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.6
```

### 2. 配置 DeepSeek API Key

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    openai:
      api-key: your-deepseek-api-key
      base-url: https://api.deepseek.com
```

### 3. 运行项目

```bash
./mvnw spring-boot:run
```

### 4. 测试 NL2SQL

```bash
# NL2SQL 查询
curl -X POST http://localhost:8080/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "查询最近一周下单的用户",
    "topK": 5,
    "sqlDialect": "mysql"
  }'
```

## API 文档

### NL2SQL 接口

#### 自然语言查询
```bash
POST /api/nl2sql/query
```

请求体：
```json
{
  "question": "查询最近一周下单的用户",
  "topK": 5,
  "sqlDialect": "mysql"
}
```

响应：
```json
{
  "success": true,
  "sql": "SELECT DISTINCT u.user_id, u.username FROM users u INNER JOIN orders o ON u.user_id = o.user_id WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)",
  "explanation": "查询最近7天有订单的用户",
  "confidence": 0.92,
  "retrievedTables": [
    {"name": "users", "description": "用户表", "similarity": 0.85},
    {"name": "orders", "description": "订单表", "similarity": 0.82}
  ],
  "extractedFields": [
    {"tableName": "users", "columnName": "user_id", "reason": "JOIN关联字段"},
    {"tableName": "users", "columnName": "username", "reason": "需要返回的用户名"},
    {"tableName": "orders", "columnName": "user_id", "reason": "JOIN关联字段"},
    {"tableName": "orders", "columnName": "created_at", "reason": "时间筛选条件"}
  ],
  "involvedTables": ["users", "orders"],
  "originalQuestion": "查询最近一周下单的用户",
  "sqlDialect": "mysql"
}
```

#### 添加表结构
```bash
POST /api/nl2sql/tables
```

请求体：
```json
{
  "name": "users",
  "description": "用户表，存储所有注册用户的基本信息",
  "columns": [
    {"name": "user_id", "dataType": "INT", "comment": "用户ID，主键", "primaryKey": true},
    {"name": "username", "dataType": "VARCHAR(50)", "comment": "用户名"}
  ],
  "usageGuide": "查询用户信息时使用"
}
```

#### 获取所有表
```bash
GET /api/nl2sql/tables
```

#### 获取表详情
```bash
GET /api/nl2sql/tables/{tableName}
```

#### 删除表
```bash
DELETE /api/nl2sql/tables/{tableName}
```

#### 获取统计信息
```bash
GET /api/nl2sql/stats
```

### RAG 接口（原有功能）

#### 上传文档
```bash
POST /api/documents
```

#### RAG 问答
```bash
POST /api/rag/query
```

## 核心设计

### 为什么表描述向量化，字段信息不向量化？

1. **精度问题**: 字段级别的语义检索容易丢失上下文
2. **完整性问题**: 用户问题可能涉及多个字段的组合关系
3. **可控性**: 通过 Agent 提取比纯向量匹配更可靠
4. **成本**: 减少向量存储和检索的复杂度

### 为什么使用两个 Agent？

1. **关注点分离**: 提取和生成是不同的认知任务
2. **上下文压缩**: Agent 1 将大量信息压缩为精简上下文
3. **可调试性**: 可以分别优化每个 Agent 的 prompt
4. **可扩展性**: 未来可插入更多 Agent（如 SQL 优化器）

## 配置说明

### SQL 方言配置

```yaml
nl2sql:
  sql-dialect: mysql  # 支持: mysql, postgresql, sqlite, oracle, sqlserver
  top-k: 5  # 默认检索表数量
```

### 向量数据库配置

```yaml
spring:
  ai:
    vectorstore:
      elasticsearch:
        index-name: rag-documents
        embedding-dimensions: 1536
        similarity: cosine
  
  elasticsearch:
    uris: http://localhost:9200
```

## 示例数据

系统启动时会自动初始化以下示例表：

- **users** - 用户表
- **orders** - 订单表
- **order_items** - 订单明细表
- **products** - 商品表
- **categories** - 商品分类表

## 测试查询示例

```bash
# 1. 查询最近一周下单的用户
curl -X POST http://localhost:8080/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"question": "查询最近一周下单的用户"}'

# 2. 统计每个用户的订单总金额
curl -X POST http://localhost:8080/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"question": "统计每个用户的订单总金额"}'

# 3. 查询订单金额超过1000的用户信息
curl -X POST http://localhost:8080/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"question": "查询订单金额超过1000的用户信息"}'

# 4. 统计最近一个月各分类的商品销售数量
curl -X POST http://localhost:8080/api/nl2sql/query \
  -H "Content-Type: application/json" \
  -d '{"question": "统计最近一个月各分类的商品销售数量"}'
```

## 开发计划

- [x] 需求分析
- [x] 系统架构设计
- [x] 核心功能实现
- [x] 集成 DeepSeek LLM
- [ ] SQL 执行和结果验证
- [ ] 用户反馈收集
- [ ] Web UI 界面
- [ ] 支持更多 SQL 方言

## 许可证

MIT
