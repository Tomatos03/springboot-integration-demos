# Elasticsearch

Elasticsearch 是一个基于 Lucene 的分布式、RESTful 风格的搜索和数据分析引擎。它能够快速、近实时地存储、搜索和分析海量数据，广泛应用于全文检索、日志分析、实时监控等场景。

---

## 目录结构

当前demo的目录结构如下：

```text
src/main/java/com/demo/elasticsearch/
├── clientapi/           # ES Client API 层
│   ├── controller/      # REST API 控制器
│   └── service/         # ES 查询服务接口及实现
│       ├── compound/    # 复合查询 (Compound queries)
│       ├── fulltext/    # 全文检索 (Full text queries)
│       ├── product/     # 基础商品操作服务 (包含 impl 实现类)
│       ├── special/     # 特殊查询 (Specialized queries)
│       └── termlevel/   # 精确查询 (Term-level queries)
├── config/              # 配置类 (ES 客户端配置)
├── constant/            # 常量定义类 (如 EsConstants)
├── dto/                 # 数据传输对象 (请求和响应封装)
├── exception/           # 全局异常处理机制
├── model/               # 实体类模型 (ES 文档映射)
└── ElasticsearchApplication.java # 启动类
```

---

## 快速开始

### 启动本地环境 (Docker)

当前 demo 准备了 Docker Compose 文件，可以一键启动 Elasticsearch + Kibana（默认单节点、免密）。

```bash
cd docs/docker
docker-compose up -d
```
服务启动后即可访问：
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`

### 启动 Spring Boot 项目

退回到仓库根目录，执行以下 Maven 命令启动 demo：

```bash
mvn -pl elasticsearch spring-boot:run
```
*应用默认在 `8083` 端口运行。*

### 初始化与测试

服务启动完毕后，可以通过如下 `curl` 命令（或使用 Postman/Apifox）进行测试：

**初始化示例数据（将自动创建索引并插入测试商品）:**
```bash
curl -X POST "http://localhost:8083/api/es/client/products/init"
```

**查询该索引下的所有文档数据:**
```bash
curl -X GET "http://localhost:8083/api/es/client/products/all"
```

**测试各种查询的 curl 示例：**

1. **全文检索 (Match Query)**
```bash
curl -X POST "http://localhost:8083/api/es/client/search/fulltext/match" \
     -H "Content-Type: application/json" \
     -d '{"keyword": "手机"}'
```

2. **精确匹配 (Term Query)**
```bash
curl -X GET "http://localhost:8083/api/es/client/search/term?field=brand&value=Apple"
```

3. **范围查询 (Range Query)**
```bash
curl -X GET "http://localhost:8083/api/es/client/search/range?minPrice=3000&maxPrice=8000"
```

4. **复合查询 (Bool Query)**
```bash
curl -X POST "http://localhost:8083/api/es/client/search/bool" \
     -H "Content-Type: application/json" \
     -d '{"keyword": "手机", "brand": "Apple", "minPrice": 3000, "maxPrice": 8000}'
```

5. **聚合分析 (Category Aggregation)**
```bash
curl -X GET "http://localhost:8083/api/es/client/products/agg/category?size=10"
```

---

## 字段数据类型

Elasticsearch 提供了丰富的字段数据类型，用于定义文档中不同字段的数据结构和索引方式。以下是常见的几种：

| 字段类型 | 说明与特点 |
| :--- | :--- |
| **`text`** | 用于**全文检索**的字符串类型。写入时会被分词器（Analyzer）解析拆分成独立的词项存入倒排索引，适用于长文本（如文章正文、商品描述）。默认不支持聚合、排序。 |
| **`keyword`** | 用于**精确匹配**的字符串类型。写入时**不会**被分词，整体作为一个完整的值存入倒排索引。适用于结构化数据（如邮箱地址、品牌、标签、状态码）。支持聚合和排序。 |
| **`byte`** | 有符号的 8 位整数，范围为 -128 到 127。 |
| **`short`** | 有符号的 16 位整数，范围为 -32,768 到 32,767。 |
| **`integer`** | 有符号的 32 位整数，范围为 -2<sup>31</sup> 到 2<sup>31</sup>-1。 |
| **`long`** | 有符号的 64 位整数，范围为 -2<sup>63</sup> 到 2<sup>63</sup>-1。 |
| **`float`** | 单精度 32 位 IEEE 754 浮点数。 |
| **`double`** | 双精度 64 位 IEEE 754 浮点数。 |
| **`half_float`** | 半精度 16 位 IEEE 754 浮点数。 |
| **`scaled_float`** | 带有缩放因子（`scaling_factor`）的浮点数，底层由 `long` 存储支持。非常适合精度要求固定且需优化存储与查询性能的场景（例如存储金额时乘以 100 存为整数）。 |
| **`date`** | 日期类型。底层以毫秒级时间戳的 `long` 型存储，支持接受格式化字符串（如 `"2023-10-01"`）或时间戳。非常适合进行时间范围查询和日期直方图聚合。 |
| **`boolean`** | 布尔类型。只接受 `true` 和 `false`。 |
| **`object`** | 普通对象类型。用于存储 JSON 对象。若存储对象数组，内部会被扁平化（Flattened）处理为多个独立的一维数组，从而丢失多层级对象数组中属性之间的关联关系。 |
| **`nested`** | 嵌套对象类型。专门用于解决 `object` 类型处理对象数组时关联关系丢失的问题。它将数组中的每个子对象作为独立的隐藏文档进行索引，允许对其进行精确的多字段联合查询。 |
| **`geo_point`** | 地理空间坐标类型。用于存储经纬度点（latitude 和 longitude），支持通过地理位置进行查找（如：查找附近 5 公里的店铺）。 |

---

## 查询类型

### 全文检索 (Full-text Queries)

- **工作原理**：通常用于查询 `text` 类型的字段。在查询执行前，ES 会将用户输入的查询词传递给分词器（Analyzer）进行分词处理，然后再去倒排索引中匹配。
- **适用场景**：搜索商品描述、文章内容、新闻标题等。
- **常用查询**：

| 查询类型 | 说明 |
| :--- | :--- |
| `match` | 标准的全文检索，会对输入内容分词。 |
| `multi_match` | 在多个字段中执行 `match` 查询。 |
| `match_phrase` | 短语查询，不仅要求包含所有分词，还要求词的顺序保持一致。 |

> **`match` vs `match_phrase`**
> 假设有一条文档的内容为："我买了一个 Apple 生产的手机"
>
> - 如果使用 **`match`** 搜索 `"Apple 手机"`：ES 会将搜索词分词为 `"Apple"` 和 `"手机"`。因为文档中同时包含了这两个词，所以**能够匹配到**（即使它们之间隔了其他词，或者顺序颠倒）。
> - 如果使用 **`match_phrase`** 搜索 `"Apple 手机"`：ES 不仅要求文档包含这两个词，还要求它们必须**紧挨着且顺序完全一致**。因为原文档中它们中间隔了 "生产的" 三个字，所以**无法匹配到**。

> [!NOTE]
> 
> **代码参考** [FulltextQueryServiceImpl](src/main/java/com/demo/elasticsearch/clientapi/service/fulltext/FulltextQueryServiceImpl.java) 类。

### 精确匹配 (Term-level Queries)

- **工作原理**：通常用于查询 `keyword`、`numeric`、`date` 等确切值的字段。ES **不会**对输入的查询条件进行分词，而是直接去倒排索引中进行精确的完全匹配。
- **适用场景**：根据商品ID、状态码、品牌名称、时间区间等进行过滤和筛选。
- **常用查询**：

| 查询类型 | 说明 |
| :--- | :--- |
| `term` | 精确匹配单个值。 |
| `terms` | 精确匹配多个值（类似于 SQL 的 `IN`）。 |
| `range` | 范围查询（如价格区间、时间区间）。 |
| `prefix` | 前缀查询。 |
| `wildcard` | 通配符查询。 |
| `fuzzy` | 模糊查询。基于编辑距离（Levenshtein distance）匹配词项，允许一定的拼写错误。 |

> **💡 什么是编辑距离 (Levenshtein distance)？**
> 编辑距离是指将一个字符串转换成另一个字符串所需的最少单字符编辑操作次数。允许的操作包括：
> - **插入**一个字符（如：`box` -> `boxes`）
> - **删除**一个字符（如：`black` -> `lack`）
> - **替换**一个字符（如：`box` -> `fox`）
> - **交换**相邻字符（如：`act` -> `cat`）
>
> 在 Elasticsearch 的 `fuzzy` 查询中，默认的模糊度（`fuzziness`）通常为 `AUTO`，允许的最大编辑距离通常为 2。这在用户输入搜索词时发生轻微拼写错误的情况下非常有用（例如把 "apple" 错拼成了 "appla"）。

> [!NOTE]
> 
> **代码参考** [TermLevelQueryServiceImpl](src/main/java/com/demo/elasticsearch/clientapi/service/termlevel/TermLevelQueryServiceImpl.java) 类。

### 条件查询 (Compound Queries)

- **工作原理**：将多个简单的查询（如 `match`、`term` 等）组合在一起，用于构建复杂的业务查询逻辑，同时可以精细化控制文档的评分（Relevance Score）。
- **核心查询 (`bool` Query)**：

  `bool` 查询是 Elasticsearch 中最常用的复合查询，它包含四种不同的子句（Occur 机制）：

| 子句类型 | 逻辑等价 | 是否必须满足 | 是否贡献算分 | 适用场景与说明 |
| :--- | :--- | :--- | :--- | :--- |
| **`must`** | 相当于 **`AND`** | ✅ 是 | ✅ 是 | **正向匹配**。文档必须满足该条件，且匹配程度越高，文档得分越高。适用于核心检索词（如搜索商品名称）。 |
| **`filter`** | 相当于 **`AND`** (不加入相关性分数计算) | ✅ 是 | ❌ 否 | **过滤条件**。文档必须满足该条件，但不计算得分。ES 会对其结果进行缓存，性能极高。适用于精确匹配、范围过滤（如筛选品牌、价格区间、状态）。 |
| **`should`** | 相当于 **`OR`** | ❓ 可选 | ✅ 是 | **可选匹配 / 偏好提升**。文档不一定要满足该条件，但如果满足了，相关性得分会增加。常用于提升特定条件的搜索排名。 |
| **`must_not`** | 相当于 **`NOT`** | ❌ 绝对不能 | ❌ 否 | **排除条件**。文档绝不能满足该条件，不计算得分，也会被放入缓存。适用于排除不需要的数据（如排除已下架商品）。 |

> **💡 `must` vs `filter` 性能差异**
> 建议在业务开发中：只要不涉及算分的条件（比如状态=1，价格>100），全部放到 `filter` 中执行，这样不仅速度快，还能有效利用 ES 的 Query Cache。

除了 `bool` 查询，常用的复合查询还包括 `constant_score`（固定分数过滤）、`boosting`（正负向降权评分）和 `dis_max`（最佳匹配字段取最大分）等。

> [!NOTE]
> 
> **代码参考** [CompoundQueryServiceImpl](src/main/java/com/demo/elasticsearch/clientapi/service/compound/CompoundQueryServiceImpl.java) 类。

### 聚合搜索 (Aggregation Queries)

- **工作原理**：不返回具体文档，而是对数据做统计分析，返回统计结果（如分组、计数、平均值等），常用于数据分析、筛选面板、统计图表等场景。
- **适用场景**：电商筛选面板、数据看板、搜索结果统计等。
- **一句话总结**：聚合搜索 = 对数据做统计分析，不返回具体文档。

| 聚合类型 | 说明 | 典型场景 |
| :--- | :--- | :--- |
| **桶聚合<br>(Bucket Aggregation)** | 按字段值或范围分组统计文档数量 | 商品分类统计、价格区间统计 |
| **指标聚合<br>(Metric Aggregation)** | 计算数值型指标，如总数、平均值、最大/最小值、求和 | 订单总额、平均价格、最高/最低分数 |
| **管道聚合<br>(Pipeline Aggregation)** | 对已有聚合结果再做聚合计算 | 统计增长率、环比/同比分析 |
| **矩阵聚合<br>(Matrix Aggregation)** | 多字段矩阵相关性、协方差等复杂计算 | 多指标相关性分析、金融风控 |

> 例如：统计每个分类商品数量、各品牌商品均价、订单趋势图等，均属于聚合搜索的典型应用。

> [!NOTE]
> 
> **代码参考** [AggregationQueryServiceImpl](src/main/java/com/demo/elasticsearch/clientapi/service/aggregation/AggregationQueryServiceImpl.java) 类。

---

### 其他高级查询 (Other/Advanced Queries)

除了上述三大核心查询，ES 还提供了许多强大的专业检索能力：

| 查询类别 | 说明 |
| :--- | :--- |
| **地理位置查询 (Geo Queries)** | 基于经纬度搜索，如查找附近的店铺（`geo_distance`）、在指定多边形内的位置（`geo_polygon`）。 |
| **嵌套查询 (Nested/Has Child)** | 用于处理复杂的关系型文档结构，如在订单中包含多个独立的子商品项。 |
| **特殊查询 (Specialized Queries)** | 例如 `more_like_this` 用于基于现有文档寻找相似的文档（推荐系统常用）。 |

> [!NOTE]
> 
> **代码参考** [SpecialQueryServiceImpl](src/main/java/com/demo/elasticsearch/clientapi/service/special/SpecialQueryServiceImpl.java) 类。