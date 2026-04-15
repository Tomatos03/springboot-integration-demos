package com.demo.elasticsearch.clientapi.service.termlevel;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.UntypedRangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.ProductSearchResponse;
import com.demo.elasticsearch.dto.ProductView;
import com.demo.elasticsearch.model.ProductDocument;
import org.springframework.stereotype.Service;
import com.demo.elasticsearch.constant.EsConstants;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class TermLevelQueryServiceImpl implements TermLevelQueryService {

    private final ElasticsearchClient client;
    private final ElasticsearchProperties properties;

    public TermLevelQueryServiceImpl(ElasticsearchClient client, ElasticsearchProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ProductSearchResponse termQuery(String field, String value) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(10)
                                                                         .query(q -> q.term(t -> t.field(field)
                                                                                                  .value(value))),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse termsQuery(String field, List<String> values) throws IOException {
        List<FieldValue> fieldValues = values.stream()
                                             .map(FieldValue::of)
                                             .toList();
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(10)
                                                                         .query(q -> q.terms(t -> t.field(field)
                                                                                                   .terms(ts -> ts.value(fieldValues)))),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse rangeQuery(BigDecimal minPrice, BigDecimal maxPrice) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(10)
                                                                         .query(q -> q
                                                                                 .range(r -> r
                                                                                         .untyped(
                                                                                                 priceRange(
                                                                                                         minPrice,
                                                                                                         maxPrice
                                                                                                 )
                                                                                         )
                                                                                 )
                                                                         ),
                                                                 ProductDocument.class
        );
        return buildResponse(response);
    }

    @Nonnull
    private static Function<UntypedRangeQuery.Builder, ObjectBuilder<UntypedRangeQuery>> priceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return untyped -> {
            untyped.field("price");
            if (minPrice != null) {
                untyped.gte(JsonData.of(minPrice));
            }
            if (maxPrice != null) {
                untyped.lte(JsonData.of(maxPrice));
            }
            return untyped;
        };
    }

    @Override
    public ProductSearchResponse prefixQuery(String field, String prefix) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(10)
                                                                         .query(q -> q
                                                                                 .prefix(p -> p
                                                                                         .field(field)
                                                                                         .value(prefix)
                                                                                         // 是否区分大小写， true表示不区分
                                                                                         .caseInsensitive(true)
                                                                                 )
                                                                         ),
                                                                 ProductDocument.class);
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse wildcardQuery(String field, String wildcard) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .query(q -> q
                                                                                 .wildcard(v -> v
                                                                                         .field(field)
                                                                                         .value(wildcard)
                                                                                 )
                                                                         ),
                                                                 ProductDocument.class
        );
        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse fuzzyQuery(String field, String value) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(10)
                                                                         .query(q -> q
                                                                                 .fuzzy(f -> f
                                                                                         .field(field)
                                                                                         .value(value)
                                                                                         .fuzziness(EsConstants.Fuzziness.AUTO)
                                                                                 )
                                                                         ),
                                                                 ProductDocument.class
        );

        return buildResponse(response);
    }

    @Override
    public ProductSearchResponse idsQuery(List<String> ids) throws IOException {
        SearchResponse<ProductDocument> response = client.search(s -> s
                                                                         .index(indexName())
                                                                         .size(10)
                                                                         .query(q -> q
                                                                                 .terms(t -> t
                                                                                         .field("id")
                                                                                         .terms(ts -> ts
                                                                                                 .value(
                                                                                                         ids.stream()
                                                                                                            .map(FieldValue::of)
                                                                                                            .toList()
                                                                                                 )
                                                                                         )
                                                                                 )
                                                                         ),
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