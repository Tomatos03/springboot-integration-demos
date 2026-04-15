package com.demo.elasticsearch.clientapi.service.product;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.*;
import com.demo.elasticsearch.exception.BizException;
import com.demo.elasticsearch.model.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class ProductElasticsearchServiceImpl implements ProductElasticsearchService {

    private static final List<String> SORTABLE_FIELDS = Arrays.asList("price", "sales", "createTime", "stock");

    private final ElasticsearchClient esClient;
    private final ElasticsearchProperties properties;

    public ProductElasticsearchServiceImpl(ElasticsearchClient esClient,
                                           ElasticsearchProperties properties) {
        this.esClient = esClient;
        this.properties = properties;
    }

    @Override
    public boolean createIndex() throws IOException {
        if (indexExists()) {
            return false;
        }
        return esClient.indices()
                       .create(builder -> builder
                               .index(indexName())
                               .mappings(mapping -> mapping
                                       .properties("id",
                                                   Property.of(prop -> prop.keyword(keyword -> keyword)))
                                       .properties(
                                               "name",
                                               Property.of(prop -> prop.text(text -> text
                                                       .fields("keyword", keywordField -> keywordField.keyword(
                                                               keyword -> keyword))
                                               )))
                                       .properties("category",
                                                   Property.of(prop -> prop.keyword(keyword -> keyword)))
                                       .properties("brand",
                                                   Property.of(prop -> prop.keyword(keyword -> keyword)))
                                       .properties("price", Property.of(prop -> prop.double_(num -> num)))
                                       .properties("sales", Property.of(prop -> prop.integer(num -> num)))
                                       .properties("stock", Property.of(prop -> prop.integer(num -> num)))
                                       .properties("tags",
                                                   Property.of(prop -> prop.keyword(keyword -> keyword)))
                                       .properties("description",
                                                   Property.of(prop -> prop.text(text -> text)))
                                       .properties("createTime",
                                                   Property.of(prop -> prop.date(date -> date)))
                               )
                               .settings(setting -> setting
                                       .numberOfShards("1")
                                       .numberOfReplicas("0")
                               )
                       )
                       .acknowledged();
    }

    @Override
    public boolean deleteIndex() throws IOException {
        if (!indexExists()) {
            return false;
        }
        return esClient.indices()
                       .delete(builder -> builder.index(indexName()))
                       .acknowledged();
    }

    @Override
    public ProductDocument upsertProduct(String id, ProductUpsertRequest request) throws IOException {
        assertIndexExists();
        ProductDocument document = buildDocument(id, request);
        esClient.index(builder -> builder
                .index(indexName())
                .id(document.getId())
                .document(document)
        );
        return document;
    }

    @Override
    public ProductDocument getProductById(String id) throws IOException {
        assertIndexExists();
        GetResponse<ProductDocument> response = esClient.get(builder -> builder
                                                                     .index(indexName())
                                                                     .id(id),
                                                             ProductDocument.class
        );
        if (!response.found()) {
            throw new BizException(404, "商品不存在: " + id);
        }
        return response.source();
    }

    @Override
    public void deleteProduct(String id) throws IOException {
        assertIndexExists();
        DeleteResponse response = esClient.delete(builder -> builder
                .index(indexName())
                .id(id)
        );
        if (response.result() == Result.NotFound) {
            throw new BizException(404, "商品不存在: " + id);
        }
    }

    @Override
    public BulkUpsertResponse bulkUpsert(BulkUpsertRequest request) throws IOException {
        assertIndexExists();
        BulkUpsertResponse result = new BulkUpsertResponse();
        if (CollectionUtils.isEmpty(request.getProducts())) {
            result.setTotal(0);
            return result;
        }

        List<ProductUpsertRequest> products = request.getProducts();
        result.setTotal(products.size());

        BulkResponse response = esClient.bulk(builder -> {
            for (ProductUpsertRequest item : products) {
                String id = StringUtils.hasText(item.getId()) ? item.getId() : UUID.randomUUID()
                                                                                   .toString();
                ProductDocument document = buildDocument(id, item);
                builder.operations(op -> op.index(action -> action
                        .index(indexName())
                        .id(id)
                        .document(document)
                ));
            }
            return builder;
        });

        int success = products.size();
        if (response.errors()) {
            for (BulkResponseItem item : response.items()) {
                if (item.error() != null) {
                    success--;
                    result.getErrors()
                          .add("id=" + item.id() + ", reason=" + item.error()
                                                                     .reason());
                }
            }
        }
        result.setSuccess(success);
        result.setFailed(products.size() - success);
        return result;
    }

    @Override
    public ProductSearchResponse search(ProductSearchRequest request) throws IOException {
        assertIndexExists();

        int page = request.getPage() == null ? 1 : request.getPage();
        int size = request.getSize() == null ? 10 : request.getSize();
        int from = (page - 1) * size;

        SearchResponse<ProductDocument> response = esClient.search(builder -> {
                                                                       builder.index(indexName())
                                                                              .from(from)
                                                                              .size(size);

                                                                       builder.query(query -> query.bool(bool -> {
                                                                           if (StringUtils.hasText(request.getKeyword())) {
                                                                               bool.must(must -> must.multiMatch(mm -> mm
                                                                                       .query(request.getKeyword())
                                                                                       .fields("name",
                                                                                               "description")
                                                                               ));
                                                                           }
                                                                           if (StringUtils.hasText(request.getCategory())) {
                                                                               bool.filter(filter -> filter.term(term -> term
                                                                                       .field("category")
                                                                                       .value(request.getCategory())
                                                                               ));
                                                                           }
                                                                           if (StringUtils.hasText(request.getBrand())) {
                                                                               bool.filter(filter -> filter.term(term -> term
                                                                                       .field("brand")
                                                                                       .value(request.getBrand())
                                                                               ));
                                                                           }
                                                                           if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                                                                               bool.filter(filter -> filter.range(range -> range.number(num -> {
                                                                                   num.field("price");
                                                                                   if (request.getMinPrice() != null) {
                                                                                       num.gte(request.getMinPrice()
                                                                                                      .doubleValue());
                                                                                   }
                                                                                   if (request.getMaxPrice() != null) {
                                                                                       num.lte(request.getMaxPrice()
                                                                                                      .doubleValue());
                                                                                   }
                                                                                   return num;
                                                                               })));
                                                                           }
                                                                           if (!CollectionUtils.isEmpty(request.getTags())) {
                                                                               List<FieldValue> values =
                                                                                       request.getTags()
                                                                                              .stream()
                                                                                              .filter(StringUtils::hasText)
                                                                                              .map(FieldValue::of)
                                                                                              .toList();
                                                                               if (!values.isEmpty()) {
                                                                                   bool.filter(filter -> filter.terms(terms -> terms
                                                                                           .field("tags")
                                                                                           .terms(term -> term.value(values))
                                                                                   ));
                                                                               }
                                                                           }
                                                                           return bool;
                                                                       }));

                                                                       if (StringUtils.hasText(request.getKeyword())) {
                                                                           builder.highlight(highlight -> highlight
                                                                                   .preTags("<em>")
                                                                                   .postTags("</em>")
                                                                                   .fields("name",
                                                                                           field -> field)
                                                                                   .fields("description",
                                                                                           field -> field)
                                                                           );
                                                                       }

                                                                       String sortField =
                                                                               normalizeSortField(request.getSortField());
                                                                       SortOrder order =
                                                                               resolveSortOrder(request.getSortOrder());
                                                                       builder.sort(sort -> sort.field(field -> field.field(sortField)
                                                                                                                     .order(order)));
                                                                       return builder;
                                                                   },
                                                                   ProductDocument.class
        );

        ProductSearchResponse result = new ProductSearchResponse();
        TotalHits totalHits = response.hits()
                                      .total();
        result.setTotal(totalHits == null ? response.hits()
                                                    .hits()
                                                    .size() : totalHits.value());
        result.setPage(page);
        result.setSize(size);

        List<ProductView> list = new ArrayList<>();
        for (Hit<ProductDocument> hit : response.hits()
                                                .hits()) {
            ProductDocument source = hit.source();
            if (source == null) {
                continue;
            }
            ProductView view = toView(source);
            Map<String, List<String>> highlight = hit.highlight();
            if (highlight != null) {
                List<String> nameHighlight = highlight.get("name");
                if (!CollectionUtils.isEmpty(nameHighlight)) {
                    view.setHighlightedName(nameHighlight.get(0));
                }
                List<String> descHighlight = highlight.get("description");
                if (!CollectionUtils.isEmpty(descHighlight)) {
                    view.setHighlightedDescription(descHighlight.get(0));
                }
            }
            list.add(view);
        }
        result.setList(list);
        return result;
    }

    @Override
    public BulkUpsertResponse initSampleData() throws IOException {
        if (!indexExists()) {
            boolean result = createIndex();
            log.info("创建索引products，结果: {}", result);
        }
        BulkUpsertRequest request = new BulkUpsertRequest();
        request.setProducts(buildSampleProducts());
        return bulkUpsert(request);
    }

    @Override
    public ProductView toView(ProductDocument document) {
        ProductView view = new ProductView();
        view.setId(document.getId());
        view.setName(document.getName());
        view.setCategory(document.getCategory());
        view.setBrand(document.getBrand());
        view.setPrice(document.getPrice());
        view.setSales(document.getSales());
        view.setStock(document.getStock());
        view.setTags(document.getTags());
        view.setDescription(document.getDescription());
        view.setCreateTime(document.getCreateTime());
        return view;
    }

    @Override
    public List<ProductDocument> searchAll() throws IOException {
        SearchResponse<ProductDocument> search = esClient.search(builder -> builder
                                                                         .index(indexName())
                                                                         .query(query -> query.matchAll(m -> m))
                                                                         .size(1000),
                                                                 ProductDocument.class
        );

        return search.hits()
                     .hits()
                     .stream()
                     .filter(Objects::nonNull)
                     .map(Hit::source)
                     .toList();
    }

    private String normalizeSortField(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return "createTime";
        }
        if (!SORTABLE_FIELDS.contains(sortField)) {
            throw new BizException("不支持的排序字段: " + sortField);
        }
        return sortField;
    }

    private SortOrder resolveSortOrder(String sortOrder) {
        if (!StringUtils.hasText(sortOrder)) {
            return SortOrder.Desc;
        }
        return "asc".equalsIgnoreCase(sortOrder) ? SortOrder.Asc : SortOrder.Desc;
    }

    private void assertIndexExists() throws IOException {
        if (!indexExists()) {
            throw new BizException(404, "索引不存在，请先创建索引: " + indexName());
        }
    }

    private ProductDocument buildDocument(String id, ProductUpsertRequest request) {
        ProductDocument document = new ProductDocument();
        document.setId(id);
        document.setName(request.getName());
        document.setCategory(request.getCategory());
        document.setBrand(request.getBrand());
        document.setPrice(request.getPrice());
        document.setSales(request.getSales());
        document.setStock(request.getStock());
        document.setTags(request.getTags());
        document.setDescription(request.getDescription());
        document.setCreateTime(request.getCreateTime() == null ? new Date() : request.getCreateTime());
        return document;
    }

    private boolean indexExists() throws IOException {
        BooleanResponse response = esClient.indices()
                                           .exists(builder -> builder.index(indexName()));
        return response.value();
    }

    private String indexName() {
        return properties.getIndex();
    }

    private List<ProductUpsertRequest> buildSampleProducts() {
        ProductUpsertRequest p1 = new ProductUpsertRequest();
        p1.setId("p-1001");
        p1.setName("iPhone 15");
        p1.setCategory("phone");
        p1.setBrand("Apple");
        p1.setPrice(BigDecimal.valueOf(5999));
        p1.setSales(3200);
        p1.setStock(150);
        p1.setTags(List.of("smartphone", "ios"));
        p1.setDescription("Apple 手机，A 系列芯片，拍照和性能均衡");
        p1.setCreateTime(new Date(System.currentTimeMillis() - 3L * 30 * 24 * 60 * 60 * 1000));

        ProductUpsertRequest p2 = new ProductUpsertRequest();
        p2.setId("p-1002");
        p2.setName("Mate 70 Pro");
        p2.setCategory("phone");
        p2.setBrand("Huawei");
        p2.setPrice(BigDecimal.valueOf(6999));
        p2.setSales(2100);
        p2.setStock(90);
        p2.setTags(List.of("smartphone", "harmonyos"));
        p2.setDescription("华为旗舰手机，影像能力突出");
        p2.setCreateTime(new Date(System.currentTimeMillis() - 2L * 30 * 24 * 60 * 60 * 1000));

        ProductUpsertRequest p3 = new ProductUpsertRequest();
        p3.setId("p-1003");
        p3.setName("Xiaomi 15");
        p3.setCategory("phone");
        p3.setBrand("Xiaomi");
        p3.setPrice(BigDecimal.valueOf(4299));
        p3.setSales(2800);
        p3.setStock(240);
        p3.setTags(List.of("smartphone", "android"));
        p3.setDescription("小米手机，主打高性价比与快充");
        p3.setCreateTime(new Date(System.currentTimeMillis() - 1L * 30 * 24 * 60 * 60 * 1000));

        ProductUpsertRequest p4 = new ProductUpsertRequest();
        p4.setId("p-1004");
        p4.setName("MacBook Air M3");
        p4.setCategory("laptop");
        p4.setBrand("Apple");
        p4.setPrice(BigDecimal.valueOf(8999));
        p4.setSales(1100);
        p4.setStock(60);
        p4.setTags(List.of("laptop", "office"));
        p4.setDescription("轻薄笔记本电脑，续航优秀");
        p4.setCreateTime(new Date(System.currentTimeMillis() - 5L * 30 * 24 * 60 * 60 * 1000));

        ProductUpsertRequest p5 = new ProductUpsertRequest();
        p5.setId("p-1005");
        p5.setName("ThinkPad X1 Carbon");
        p5.setCategory("laptop");
        p5.setBrand("Lenovo");
        p5.setPrice(BigDecimal.valueOf(9999));
        p5.setSales(800);
        p5.setStock(35);
        p5.setTags(List.of("laptop", "business"));
        p5.setDescription("商务办公笔记本，键盘手感出色");
        p5.setCreateTime(new Date(System.currentTimeMillis() - 4L * 30 * 24 * 60 * 60 * 1000));

        ProductUpsertRequest p6 = new ProductUpsertRequest();
        p6.setId("p-1006");
        p6.setName("Sony WH-1000XM5");
        p6.setCategory("headphone");
        p6.setBrand("Sony");
        p6.setPrice(BigDecimal.valueOf(2299));
        p6.setSales(4300);
        p6.setStock(380);
        p6.setTags(List.of("audio", "noise-canceling"));
        p6.setDescription("头戴式降噪耳机，音质优秀");
        p6.setCreateTime(new Date(System.currentTimeMillis() - 6L * 30 * 24 * 60 * 60 * 1000));

        return List.of(p1, p2, p3, p4, p5, p6);
    }
}
