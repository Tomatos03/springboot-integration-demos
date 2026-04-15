package com.demo.elasticsearch.clientapi.service.aggregation;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.CategoryBucket;
import com.demo.elasticsearch.dto.TimeBucket;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 聚合查询 (Aggregation) 服务实现类.
 */
@Service
public class AggregationQueryServiceImpl implements AggregationQueryService {

    private final ElasticsearchClient esClient;
    private final ElasticsearchProperties properties;

    public AggregationQueryServiceImpl(ElasticsearchClient esClient, ElasticsearchProperties properties) {
        this.esClient = esClient;
        this.properties = properties;
    }

    @Override
    public List<CategoryBucket> aggregateByCategory(int size) throws IOException {
        // 实际案例：词项聚合 (Terms Aggregation)
        // 场景：在电商系统中，我们需要在页面左侧或首页展示当前商城中所有的商品分类，并且显示每个分类下有多少件商品。
        // 原理：类似 SQL 中的 `SELECT category, COUNT(*) FROM products GROUP BY category`。
        // ES 会遍历文档倒排索引（或正排索引 DocValues），对指定的 category 字段进行分组统计，返回数量最多的前 `size` 个分类。
        
        // 注意点：这里的 size(0) 是外层 Search 的 size，因为我们在做聚合统计，并不关心具体的商品列表，
        // 把结果集大小设为 0 可以极大减少网络传输开销，提升聚合性能。
        SearchResponse<Void> response = esClient.search(builder -> builder
                .index(indexName())
                .size(0) // 不返回具体文档
                .aggregations("categoryAgg", agg -> agg
                        .terms(terms -> terms
                                .field("category") // 按照哪个字段分组（必须是 keyword 或有 doc_values 的数值/日期字段）
                                .size(size)        // 聚合结果最多返回多少个分类（桶）
                        )), Void.class);

        // 解析聚合响应
        Aggregate aggregate = response.aggregations().get("categoryAgg");
        
        // 因为按字符串分组，底层属于 sterms (String Terms)
        if (aggregate == null || !aggregate.isSterms()) {
            return List.of();
        }

        List<CategoryBucket> buckets = new ArrayList<>();
        // 遍历所有词项桶 (Buckets)
        for (StringTermsBucket bucket : aggregate.sterms().buckets().array()) {
            buckets.add(new CategoryBucket(bucket.key().stringValue(), bucket.docCount()));
        }
        return buckets;
    }

    @Override
    public List<TimeBucket> aggregateByMonth() throws IOException {
        // 实际案例：日期直方图聚合 (Date Histogram Aggregation)
        // 场景：在后台管理系统中，我们需要绘制一张折线图，展示过去一年每个月新增的商品数量趋势。
        // 原理：ES 会根据日期的固定日历区间（年、月、日、小时等）自动切割出时间桶（Buckets）。
        // 然后将各个商品的创建时间落入对应的时间桶内并计算文档总数。
        
        SearchResponse<Void> response = esClient.search(builder -> builder
                .index(indexName())
                .size(0) // 同样，只关心聚合结果，不返回文档
                .aggregations("monthAgg", agg -> agg.dateHistogram(hist -> hist
                        .field("createTime")                    // 按照哪个时间字段统计
                        .calendarInterval(CalendarInterval.Month) // 按自然月 (Month) 划分时间桶
                        .format("yyyy-MM")                      // 返回结果时时间桶 key 的格式化呈现
                )), Void.class);

        // 解析聚合响应
        Aggregate aggregate = response.aggregations().get("monthAgg");
        
        if (aggregate == null || !aggregate.isDateHistogram()) {
            return List.of();
        }

        List<TimeBucket> buckets = new ArrayList<>();
        // 遍历所有时间桶 (Date Histogram Buckets)
        for (DateHistogramBucket bucket : aggregate.dateHistogram().buckets().array()) {
            // keyAsString 是按照我们上面指定的 yyyy-MM 格式化出来的字符串，docCount 就是该月的数量
            buckets.add(new TimeBucket(bucket.keyAsString(), bucket.docCount()));
        }
        return buckets;
    }

    @Override
    public long maxMonthlyDocCount() throws IOException {
        // 管道聚合示例：统计每月订单数量的最大值（Max Bucket Pipeline Aggregation）
        // 1. 先做 date_histogram 按月分组
        // 2. 再用 max_bucket 管道聚合，找出最大月订单数
        SearchResponse<Void> response = esClient.search(builder -> builder
                .index(indexName())
                .size(0)
                .aggregations("monthlyOrders", agg -> agg
                        .dateHistogram(dh -> dh
                                .field("createTime")
                                .calendarInterval(CalendarInterval.Month)
                                .format("yyyy-MM")
                        )
                )
                .aggregations("maxMonthlyDocCount", agg -> agg
                        .maxBucket(mb -> mb
                                // bucketsPath 用于指定管道聚合的数据来源路径 (Buckets Path Syntax)
                                // "monthlyOrders>_count" 的含义：
                                // 1. monthlyOrders: 兄弟聚合的名称 (即上面的 dateHistogram 聚合)
                                // 2. > : 路径分隔符，用于进入子聚合或指标
                                // 3. _count: ES 内置的特殊路径，代表获取该桶的文档数量 (doc_count)
                                .bucketsPath(bp -> bp.single("monthlyOrders>_count"))
                        )
                ),
                Void.class
        );

        Aggregate maxBucketAgg = response.aggregations().get("maxMonthlyDocCount");
        if (maxBucketAgg != null && maxBucketAgg.isBucketMetricValue()) {
            return (long) maxBucketAgg.bucketMetricValue().value();
        }
        return 0L;
    }

    private String indexName() {
        return properties.getIndex();
    }
}