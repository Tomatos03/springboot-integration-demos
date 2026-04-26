package com.demo.elasticsearch.clientapi.service.highlight;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.demo.elasticsearch.clientapi.service.product.ProductElasticsearchServiceImpl;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.HighlightSearchRequest;
import com.demo.elasticsearch.dto.HighlightSearchResponse;
import com.demo.elasticsearch.dto.ProductView;
import com.demo.elasticsearch.model.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 高亮搜索服务实现类
 *
 * 使用 Elasticsearch Java Client 的 Lambda 风格 API 实现高亮搜索功能
 * 核心特性：
 * - 支持多字段高亮
 * - 支持自定义高亮标签（preTags/postTags）
 * - 支持多种查询类型（match、multi_match 等）
 * - 自动提取高亮内容并关联到原始文档
 *
 * @author Demo
 */
@Service
@RequiredArgsConstructor
public class HighlightSearchServiceImpl implements HighlightSearchService {

    private final ElasticsearchClient client;
    private final ElasticsearchProperties properties;
    private final ProductElasticsearchServiceImpl productElasticsearchService;

    /**
     * 执行高亮全文搜索
     * 
     * 使用 Lambda 风格 API 构建查询请求，支持以下特性：
     * 1. 多字段搜索：支持同时搜索多个字段（如 name 和 description）
     * 2. 高亮显示：对匹配的关键词进行 HTML 标签包裹
     * 3. 自定义标签：支持自定义高亮前置和后置标签
     * 4. 结果整合：将高亮内容与原始文档关联
     * 
     * @param request 高亮搜索请求对象
     *        - keyword: 搜索关键词（必填）
     *        - fields: 搜索字段数组，为空时默认搜索 name 和 description
     *        - preTags: 高亮前置标签，默认 "<em>"
     *        - postTags: 高亮后置标签，默认 "</em>"
     *        - size: 返回结果数量，默认 10
     * @return 高亮搜索响应对象，包含总数、分页信息和结果列表
     * @throws IOException 当 Elasticsearch 操作失败时抛出异常
     */
    @Override
    public HighlightSearchResponse highlightSearch(HighlightSearchRequest request) throws IOException {
        // 获取搜索参数
        String keyword = request.getKeyword();
        String[] fields = request.getFields() != null && request.getFields().length > 0 
            ? request.getFields() 
            : new String[]{"name", "description"};
        String preTags = request.getPreTags();
        String postTags = request.getPostTags();
        int size = request.getSize() > 0 ? request.getSize() : 10;

        // 使用 Lambda 风格 API 执行高亮搜索
        // 关键点：
        // 1. s -> s.index() 设置索引名称
        // 2. .size() 设置返回结果数量
        // 3. .query() 使用 multi_match 查询支持多字段搜索
        // 4. .highlight() 配置高亮参数
        SearchResponse<ProductDocument> response = client.search(s -> s
                // 设置目标索引
                .index(indexName())
                // 设置返回结果数量
                .size(size)
                // 配置查询条件 - 使用 multi_match 支持多字段全文搜索
                .query(q -> q.multiMatch(m -> m
                        // 设置搜索关键词
                        .query(keyword)
                        // 为每个字段配置高亮
                        .fields(fields[0], fields.length > 1 ? fields[1] : null)
                ))
                // 配置高亮参数 - Lambda 风格的关键写法
                .highlight(h -> h
                        // 全局高亮标签配置
                        .preTags(preTags)
                        .postTags(postTags)
                        // 逐字段配置高亮
                        // 对 name 字段进行高亮
                        .fields("name", f -> f
                                // 片段大小，0 表示返回整个字段
                                .fragmentSize(0)
                                // 返回片段数量
                                .numberOfFragments(1)
                        )
                        // 对 description 字段进行高亮
                        .fields("description", f -> f
                                // 设置片段大小为 150 字符
                                .fragmentSize(150)
                                // 最多返回 3 个片段
                                .numberOfFragments(3)
                        )
                ),
                // 指定返回的文档类型
                ProductDocument.class
        );

        // 解析响应并构建结果对象
        return buildHighlightResponse(response);
    }

    /**
     * 构建高亮搜索响应
     * 
     * 从 Elasticsearch 响应中提取数据，将高亮内容与原始文档关联
     * 
     * @param response Elasticsearch 原始搜索响应
     * @return 封装后的高亮搜索响应对象
     */
    private HighlightSearchResponse buildHighlightResponse(SearchResponse<ProductDocument> response) {
        HighlightSearchResponse result = new HighlightSearchResponse();
        
        // 提取总记录数
        TotalHits totalHits = response.hits().total();
        result.setTotal(totalHits == null ? response.hits().hits().size() : totalHits.value());
        result.setPage(1);
        result.setSize(response.hits().hits().size());

        // 处理每个搜索结果
        List<ProductView> list = new ArrayList<>();
        for (Hit<ProductDocument> hit : response.hits().hits()) {
            ProductDocument source = hit.source();
            if (source != null) {
                // 将文档转换为视图对象，并关联高亮内容
                ProductView view = toViewWithHighlight(source, hit.highlight());
                list.add(view);
            }
        }
        result.setList(list);
        return result;
    }

    /**
     * 将产品文档转换为视图对象，并关联高亮内容
     * 
     * 高亮内容的获取方式：
     * hit.highlight() 返回一个 Map，key 是字段名，value 是高亮后的内容列表
     * 例如：{"name": ["<em>iPhone</em>"], "description": ["...this is a great <em>phone</em>..."]}
     * 
     * @param doc 原始产品文档
     * @param highlightMap 高亮内容 Map（字段名 -> 高亮内容列表）
     * @return 包含高亮内容的产品视图对象
     */
    private ProductView toViewWithHighlight(ProductDocument doc, Map<String, List<String>> highlightMap) {
        ProductView view = new ProductView();
        
        // 复制基本属性
        view.setId(doc.getId());
        view.setName(doc.getName());
        view.setCategory(doc.getCategory());
        view.setBrand(doc.getBrand());
        view.setPrice(doc.getPrice());
        view.setSales(doc.getSales());
        view.setStock(doc.getStock());
        view.setTags(doc.getTags());
        view.setDescription(doc.getDescription());
        view.setCreateTime(doc.getCreateTime());

        // 提取并设置高亮内容
        // 如果存在高亮内容，优先使用；否则使用原始文本
        if (highlightMap != null && !highlightMap.isEmpty()) {
            // 获取 name 字段的高亮内容（取第一个片段）
            if (highlightMap.containsKey("name") && !highlightMap.get("name").isEmpty()) {
                view.setHighlightedName(highlightMap.get("name").get(0));
            } else {
                view.setHighlightedName(doc.getName());
            }
            
            // 获取 description 字段的高亮内容（将所有片段用 "..." 连接）
            if (highlightMap.containsKey("description") && !highlightMap.get("description").isEmpty()) {
                List<String> fragments = highlightMap.get("description");
                view.setHighlightedDescription(String.join("...", fragments));
            } else {
                view.setHighlightedDescription(doc.getDescription());
            }
        } else {
            // 没有高亮内容时使用原始文本
            view.setHighlightedName(doc.getName());
            view.setHighlightedDescription(doc.getDescription());
        }

        return view;
    }

    /**
     * 获取 Elasticsearch 索引名称
     * 
     * @return 配置的索引名称
     */
    private String indexName() {
        return properties.getIndex();
    }
}