# Spring AI RAG Project

基于 Spring AI + Spring Boot 的 RAG (Retrieval-Augmented Generation) 项目，使用 Elasticsearch 8.17.6 作为向量数据库，接入 DeepSeek 大模型。

## 技术栈

- Spring Boot 3.2.x
- Spring AI 0.8.x
- Elasticsearch 8.17.6 (向量数据库)
- DeepSeek (大语言模型)

## 项目结构

```
spring-ai-rag/
├── src/
│   ├── main/
│   │   ├── java/com/example/rag/
│   │   │   ├── SpringAiRagApplication.java
│   │   │   ├── config/
│   │   │   │   ├── ElasticsearchConfig.java
│   │   │   │   └── AiConfig.java
│   │   │   ├── controller/
│   │   │   │   └── RagController.java
│   │   │   ├── service/
│   │   │   │   ├── DocumentService.java
│   │   │   │   ├── EmbeddingService.java
│   │   │   │   └── RagService.java
│   │   │   ├── repository/
│   │   │   │   └── DocumentRepository.java
│   │   │   └── entity/
│   │   │       └── Document.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── prompts/
│   │           └── rag-system-prompt.st
│   └── test/
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

### 2. 配置应用

编辑 `src/main/resources/application.yml`，配置 DeepSeek API Key。

### 3. 运行项目

```bash
./mvnw spring-boot:run
```

### 4. API 接口

- **上传文档**: `POST /api/documents`
- **RAG 问答**: `POST /api/rag/query`

## API 示例

### 上传文档

```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring AI 文档",
    "content": "Spring AI 是一个用于简化 AI 应用开发的框架..."
  }'
```

### RAG 查询

```bash
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是 Spring AI？"
  }'
```
