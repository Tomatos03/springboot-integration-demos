package com.demo.elasticsearch.clientapi.service.special;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.ProductSearchResponse;
import com.demo.elasticsearch.dto.ProductView;
import com.demo.elasticsearch.model.ProductDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpecialQueryServiceImpl implements SpecialQueryService {

    private final ElasticsearchClient client;
    private final ElasticsearchProperties properties;

    public SpecialQueryServiceImpl(ElasticsearchClient client, ElasticsearchProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ProductSearchResponse moreLikeThisQuery(String field, String likeText) throws IOException {
        // 实际案例：基于文本的 "猜你喜欢" (More Like This Query)
        // 场景：在商品详情页底部，展示与当前商品描述或名称高度相似的其他商品。
        // 原理：自动提取输入文本（或现有文档）中的特征关键词（基于 TF-IDF），然后在指定字段中查找包含这些特征词的相似文档。
        // minTermFreq(1) 和 minDocFreq(1) 用于降低词频阈值（默认通常较高），保证在小数据量的 demo 中也能搜出结果。
        SearchResponse<ProductDocument> response = client.search(s -> s
                        .index(indexName())
                        .size(10)
                        .query(q -> q.moreLikeThis(mlt -> mlt
                                .fields(field)
                                .like(List.of(co.elastic.clients.elasticsearch._types.query_dsl.Like.of(l -> l.text(likeText))))
                                .minTermFreq(1)
                                .minDocFreq(1)
                        )),
                ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse scriptedMetricQuery() throws IOException {
        // 实际案例：脚本评分查询 (Script Score Query)
        // 场景：需要加入高度定制化的复杂算分逻辑。例如我们希望根据商品的"价格"来进行打分，价格越低，得分越高（性价比优先）。
        // 原理：在基础查询（如 match_all）筛选出的文档范围上，使用 ES 内置的 Painless 脚本语言，动态读取文档内部字段并计算出最终得分。
        SearchResponse<ProductDocument> response = client.search(s -> s
                        .index(indexName())
                        .size(10)
                        .query(q -> q.scriptScore(ss -> ss
                                .query(q2 -> q2.matchAll(m -> m))
                                .script(sc -> sc
                                        // 简单脚本示例：价格大于0时，得分为 1000/价格，这样价格越低排名越靠前
                                        .source("doc['price'].size() > 0 && doc['price'].value > 0 ? 1000.0 / doc['price'].value : 0")
                                )
                        )),
                ProductDocument.class);
        return buildResponse(response);
    }

    private ProductSearchResponse buildResponse(SearchResponse<ProductDocument> response) {
        ProductSearchResponse result = new ProductSearchResponse();
        TotalHits totalHits = response.hits().total();
        result.setTotal(totalHits == null ? response.hits().hits().size() : totalHits.value());
        result.setPage(1);
        result.setSize(10);

        List<ProductView> list = new ArrayList<>();
        for (Hit<ProductDocument> hit : response.hits().hits()) {
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

    private String indexName() {
        return properties.getIndex();
    }
}