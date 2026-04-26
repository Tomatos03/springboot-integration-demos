# Elasticsearch Module AGENTS.md

## Module Overview

Spring Boot 3.x 集成 Elasticsearch 8.x 的 Demo 项目，提供完整的 ES Client API 使用示例。

## Tech Stack

- Spring Boot 3.5.3
- Elasticsearch Java Client 8.18.8
- Lombok
- Jakarta Validation

## Build & Run

```bash
# 编译
mvn compile

# 运行测试
mvn test

# 启动应用 (需先启动 ES)
mvn -pl elasticsearch spring-boot:run
```

应用默认端口: **8083**

## Package Structure

```
src/main/java/com/demo/elasticsearch/
├── clientapi/
│   ├── controller/      # REST API 控制器
│   └── service/         # ES 查询服务
│       ├── autocomplete/ # 自动补全服务
│       ├── product/      # 商品 CRUD 服务
│       ├── fulltext/     # 全文检索
│       ├── termlevel/    # 精确查询
│       ├── compound/     # 复合查询
│       ├── aggregation/  # 聚合查询
│       └── special/      # 特殊查询
├── config/              # ES 客户端配置
├── constant/            # 常量
├── dto/                 # 数据传输对象
├── exception/           # 异常处理
└── model/               # 实体模型
```

## Code Conventions

1. **使用 Lombok** 简化 DTO/Entity
   - `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`
2. **Service 层**: 接口 + 实现类模式
3. **Controller 返回**: `ApiResponse<T>`
4. **索引名称**: 通过 `ElasticsearchProperties` 配置

## Key Services

| Service | Description |
|---------|-------------|
| ProductElasticsearchService | 商品 CRUD、搜索 |
| AutocompleteService | 自动补全 |
| FulltextQueryService | 全文检索 |
| TermLevelQueryService | 精确查询 |
| CompoundQueryService | 复合查询 |
| AggregationQueryService | 聚合分析 |

## Test

测试类位于: `src/test/java/com/demo/elasticsearch/`

## Important Notes

- 启动前需确保 Elasticsearch 服务运行: `cd docs/docker && docker-compose up -d`
- ES 默认地址: `http://localhost:9200`
- 初始化示例数据: `POST /api/es/client/products/init`