package com.demo.elasticsearch.clientapi.service.compound;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.ProductSearchRequest;
import com.demo.elasticsearch.dto.ProductSearchResponse;
import com.demo.elasticsearch.dto.ProductView;
import com.demo.elasticsearch.model.ProductDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompoundQueryServiceImpl implements CompoundQueryService {

    private final ElasticsearchClient client;
    private final ElasticsearchProperties properties;

    public CompoundQueryServiceImpl(ElasticsearchClient client, ElasticsearchProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ProductSearchResponse boolQuery(ProductSearchRequest request) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(getPageSize(request))
                                                                         .from(getFrom(request))
                                                                         .query(buildBoolQueryCondition(request)),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse boostingQuery(ProductSearchRequest request) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(getPageSize(request))
                                                                         .from(getFrom(request))
                                                                         .query(buildBoostingQueryCondition(request)),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse constantScoreQuery(ProductSearchRequest request) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(getPageSize(request))
                                                                         .from(getFrom(request))
                                                                         .query(buildConstantScoreQueryCondition(request)),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse disMaxQuery(ProductSearchRequest request) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(getPageSize(request))
                                                                         .from(getFrom(request))
                                                                         .query(buildDisMaxQueryCondition(request)),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    private Query buildDisMaxQueryCondition(ProductSearchRequest request) {
        // 实际案例：最佳匹配查询 (Dis Max Query)
        // 场景：用户搜索 "iPhone 13"，我们希望在一个字段（如 name）中完全匹配的商品得分更高，
        // 而不是在 name 中匹配 "iPhone" 且在 description 中匹配 "13" 的商品得分相加后更高。
        // 原理：它不会简单地将多个 match 的分数相加（bool should 会相加），而是取匹配度最高（分数最大）的那个字段的分数作为主得分。
        // tieBreaker(0.3)：对于其他也匹配了的字段，给与 30% 的额外分数补偿。
        return Query.of(q -> q
                .disMax(dm -> dm
                        .queries(List.of(
                                Query.of(q1 -> q1.match(m -> m.field("name")
                                                              .query(request.getKeyword() != null ?
                                                                             request.getKeyword() : ""))),
                                Query.of(q2 -> q2.match(m -> m.field("description")
                                                              .query(request.getKeyword() != null ?
                                                                             request.getKeyword() : "")))
                        ))
                        .tieBreaker(0.3)
                ));
    }

    private Query buildConstantScoreQueryCondition(ProductSearchRequest request) {
        // 实际案例：固定分数查询 (Constant Score Query)
        // 场景：在某些不需要文本相关性打分（TF-IDF算法）的场景中，只要满足条件就给一个固定的基础分数。
        // 比如：我们在做运营侧的商品置顶/推荐，只要是选定品牌的商品，我们就固定给 1.5 分的基础权重。
        // 原理：它将任何查询（通常是 filter）转换成分数固定的查询，完全跳过了底层计算文本相关性带来的性能开销。
        return Query.of(q -> q
                .constantScore(cs -> cs
                        .filter(f -> f.term(t -> t.field("brand")
                                                  .value(request.getBrand() != null ? request.getBrand() : "")))
                        .boost(1.5f)
                ));
    }

    private Query buildBoostingQueryCondition(ProductSearchRequest request) {
        // 实际案例：降权查询 (Boosting Query)
        // 场景：用户搜索 "手机"，我们希望主要展示手机，但结果里可能会匹配出 "手机壳"、"手机贴膜" 等配件。
        // 此时我们不想用 must_not 暴力把配件屏蔽掉（有时候用户可能也想顺便看看），而是把它们的排名"降级"。
        // 原理：匹配 positive 条件的文档会获得正常的 TF-IDF 算分；如果它同时也匹配了 negative 条件，则其最终得分 = 原得分 * negativeBoost。
        // 例如下方例子：遇到类别是 "Accessories" (配件) 的商品，其得分会乘以 0.2 被大幅降权，从而自然地排在正经手机的后面。
        return Query.of(q -> q.boosting(b -> b
                .positive(p -> p.match(m -> m.field("name")
                                             .query(request.getKeyword() != null ? request.getKeyword() : "")))
                .negative(n -> n.term(t -> t.field("category")
                                            .value("Accessories")))
                .negativeBoost(0.2f)
        ));
    }

    private String indexName() {
        return properties.getIndex();
    }

    private int getPageSize(ProductSearchRequest request) {
        return request.getSize() != null ? request.getSize() : 10;
    }

    private int getFrom(ProductSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 1;
        return Math.max(0, (page - 1) * getPageSize(request));
    }

    private Query buildBoolQueryCondition(ProductSearchRequest request) {
        // 实际案例：多条件组合查询 (Bool Query)
        // 场景：电商系统中最常见的、带有筛选条件的搜索栏。
        // 原理与最佳实践：
        // 1. must：用于必须匹配，且需要影响相关性算分的核心检索（如用户输入的 keyword 去全文检索商品名称或描述）。
        // 2. filter：用于硬性过滤条件（如选了分类、品牌、价格区间）。放进 filter 中的条件绝对不参与算分，速度快且能利用 ES 的内部缓存。
        return Query.of(q -> q.bool(b -> {
            if (request.getKeyword() != null && !request.getKeyword()
                                                        .isEmpty()) {
                b.must(m -> m.multiMatch(mm -> mm.fields("name", "description")
                                                 .query(request.getKeyword())));
            }
            if (request.getCategory() != null && !request.getCategory()
                                                         .isEmpty()) {
                b.filter(f -> f.term(t -> t.field("category")
                                           .value(request.getCategory())));
            }
            if (request.getBrand() != null && !request.getBrand()
                                                      .isEmpty()) {
                b.filter(f -> f.term(t -> t.field("brand")
                                           .value(request.getBrand())));
            }
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                b.filter(f -> f.range(r -> r.untyped(u -> {
                    u.field("price");
                    if (request.getMinPrice() != null) {
                        u.gte(JsonData.of(request.getMinPrice()));
                    }
                    if (request.getMaxPrice() != null) {
                        u.lte(JsonData.of(request.getMaxPrice()));
                    }
                    return u;
                })));
            }
            if (request.getTags() != null && !request.getTags()
                                                     .isEmpty()) {
                List<FieldValue> tagValues = request.getTags()
                                                    .stream()
                                                    .map(FieldValue::of)
                                                    .toList();
                b.filter(f -> f.terms(t -> t.field("tags")
                                            .terms(ts -> ts.value(tagValues))));
            }
            return b;
        }));
    }

    private ProductSearchResponse buildResponse(SearchResponse<ProductDocument> response) {
        ProductSearchResponse result = new ProductSearchResponse();
        TotalHits totalHits = response.hits()
                                      .total();
        result.setTotal(totalHits == null ? response.hits()
                                                    .hits()
                                                    .size() : totalHits.value());
        result.setPage(1);
        result.setSize(10);

        List<ProductView> list = new ArrayList<>();
        for (Hit<ProductDocument> hit : response.hits()
                                                .hits()) {
            ProductDocument source = hit.source();
            if (source != null) {
                list.add(toView(source));
            }
        }
        result.setList(list);
        return result;
    }

    private ProductView toView(ProductDocument doc) {
        ProductView view = new ProductView();
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
        return view;
    }
}