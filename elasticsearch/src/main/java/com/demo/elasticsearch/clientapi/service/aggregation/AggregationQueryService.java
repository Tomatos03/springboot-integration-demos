package com.demo.elasticsearch.clientapi.service.aggregation;

import com.demo.elasticsearch.dto.CategoryBucket;
import com.demo.elasticsearch.dto.TimeBucket;

import java.io.IOException;
import java.util.List;

/**
 * Elasticsearch 聚合查询 (Aggregation) 服务接口.
 * <p>
 * 聚合查询用于对数据进行统计分析，类似于 SQL 中的 GROUP BY。
 * Elasticsearch 的聚合不仅速度极快，还能在搜索结果集（由 Query 过滤后）之上动态计算分类、分桶、指标（如最大最小平均值）等。
 */
public interface AggregationQueryService {

    /**
     * 按照商品分类进行词项聚合统计 (Terms Aggregation).
     * 实际案例：电商首页展示各个分类下有多少件商品。
     *
     * @param size 聚合返回的最大桶(类别)数量
     * @return 分类统计桶集合
     * @throws IOException ES通信异常
     */
    List<CategoryBucket> aggregateByCategory(int size) throws IOException;

    /**
     * 按照商品创建时间进行日期直方图聚合统计 (Date Histogram Aggregation).
     * 实际案例：后台大屏展示每月新增商品数量折线图。
     *
     * @return 月度统计桶集合
     * @throws IOException ES通信异常
     */
    List<TimeBucket> aggregateByMonth() throws IOException;

    /**
     * 管道聚合示例：统计每月订单数量的最大值（Max Bucket Pipeline Aggregation）。
     * 实际案例：找出一年中订单量最高的月份及其数量。
     *
     * @return 最大桶的文档数（如最大月订单数）
     * @throws IOException ES通信异常
     */
    long maxMonthlyDocCount() throws IOException;
}