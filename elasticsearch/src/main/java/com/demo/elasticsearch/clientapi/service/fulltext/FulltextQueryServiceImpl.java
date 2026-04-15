package com.demo.elasticsearch.clientapi.service.fulltext;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.ProductSearchRequest;
import com.demo.elasticsearch.dto.ProductSearchResponse;
import com.demo.elasticsearch.dto.ProductView;
import com.demo.elasticsearch.model.ProductDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Service
public class FulltextQueryServiceImpl implements FulltextQueryService {

    private final ElasticsearchClient client;
    private final ElasticsearchProperties properties;

    public FulltextQueryServiceImpl(ElasticsearchClient client, ElasticsearchProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ProductSearchResponse matchQuery(ProductSearchRequest request) throws IOException {
        String keyword = request.getKeyword();
        SearchResponse<ProductDocument> response = client.search(s -> s.index(indexName())
                                                                       .size(10)
                                                                       .query(q -> q
                                                                               .match(m -> m
                                                                                       .field("description")
                                                                                       .query(keyword)
                                                                               )
                                                                       ),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse multiMatchQuery(ProductSearchRequest request) throws IOException {
        String keyword = request.getKeyword();
        // multi_match 查询会在多个字段上进行全文检索，没有指定匹配字段默认尝试匹配全部字段
        SearchResponse<ProductDocument> response = client.search(s -> s.index(indexName())
                                                                       .size(10)
                                                                       .query(q -> q.multiMatch(m -> m.query(keyword))),
                                                                 ProductDocument.class);
        // 如果需要指定多个字段，可以使用 fields 方法，例如：
        // .query(q -> q.multiMatch(m -> m.query(keyword).fields("name", "description")))
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse matchPhraseQuery(ProductSearchRequest request) throws IOException {
        // 对于keyword使用空格区分多个词
        String keyword = request.getKeyword();
        SearchResponse<ProductDocument> response = client.search(s -> s.index(indexName())
                                                                       .size(10)
                                                                       .query(q -> q.matchPhrase(
                                                                               m -> m.field(keyword))),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse queryStringQuery(ProductSearchRequest request) throws IOException {
        String keyword = request.getKeyword();
        SearchResponse<ProductDocument> response = client.search(s -> s.index(indexName())
                                                                       .size(10)
                                                                       .query(q -> q.queryString(
                                                                               qs -> qs.query(keyword))),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    private String indexName() {
        return properties.getIndex();
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