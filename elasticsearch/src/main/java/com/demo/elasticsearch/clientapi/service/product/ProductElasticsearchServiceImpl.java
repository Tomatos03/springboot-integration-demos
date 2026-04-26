package com.demo.elasticsearch.clientapi.service.product;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
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
    public boolean createIndexIfNotExist() throws IOException {
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
            boolean result = createIndexIfNotExist();
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
        List<ProductUpsertRequest> products = new ArrayList<>();

        String[] categories = {"phone", "laptop", "headphone", "tablet", "smartwatch", "camera", "monitor", "keyboard", "mouse", "speaker"};
        String[][] categoryTags = {
            {"smartphone", "ios"}, {"smartphone", "harmonyos"}, {"smartphone", "android"},
            {"laptop", "office"}, {"laptop", "business"}, {"laptop", "gaming"}, {"laptop", "creative"},
            {"audio", "noise-canceling"}, {"audio", "wireless"}, {"audio", "hifi"},
            {"tablet", "ios"}, {"tablet", "android"}, {"tablet", "productivity"},
            {"smartwatch", "fitness"}, {"smartwatch", "health"}, {"smartwatch", "luxury"},
            {"camera", "dslr"}, {"camera", "mirrorless"}, {"camera", "action"},
            {"monitor", "gaming"}, {"monitor", "professional"}, {"monitor", "4k"},
            {"keyboard", "mechanical"}, {"keyboard", "membrane"}, {"keyboard", "wireless"},
            {"mouse", "gaming"}, {"mouse", "office"}, {"mouse", "vertical"},
            {"speaker", "bluetooth"}, {"speaker", "smart"}, {"speaker", "portable"}
        };
        String[] brands = {"Apple", "Huawei", "Xiaomi", "Lenovo", "Sony", "Samsung", "OPPO", "vivo", "ASUS", "Dell", "Microsoft", "Google", "OnePlus", "Realme", "Acer", "HP", "Bose", "JBL", "Logitech", "Razer", "SteelSeries"};
        String[] descriptions = {
            "旗舰级产品，性能卓越", "性价比之选，值得拥有", "专业级设备，满足需求",
            "轻薄设计，便于携带", "续航持久，使用安心", "影像能力出众，拍照清晰",
            "音质优秀，沉浸体验", "屏幕素质高，观感舒适", "散热出色，运行稳定",
            "外观时尚，做工精细"
        };
        Random random = new Random(42);

        String[] modelSuffixes = {
            "Pro Max", "Pro", "Ultra", "Ultimate", "Elite",
            "Air", "Lite", "Mini", "Max", "Plus",
            "Slim", "Classic", "Special Edition", "Power", "Studio",
            "Master", "Neo", "Prime", "Sport", "Extreme"
        };
        String[] colors = {"Space Black", "Silver", "Starlight", "Midnight", "Graphite",
            "Gold", "Deep Purple", "Sierra Blue", "Alpine Green", "Red",
            "White", "Gray", "Blue", "Pink", "Purple",
            "Orange", "Yellow", "Titanium", "Obsidian", "Ivory"
        };
        // Used to guarantee unique product names
        java.util.HashSet<String> usedNames = new java.util.HashSet<>();

        for (int i = 0; i < 50; i++) {
            ProductUpsertRequest product = new ProductUpsertRequest();
            int categoryIndex = random.nextInt(categories.length);
            String category = categories[categoryIndex];
            product.setId("p-" + (1001 + i));
            product.setCategory(category);

            int brandIndex = random.nextInt(brands.length);
            product.setBrand(brands[brandIndex]);

            int tagSetIndex = random.nextInt(categoryTags.length);
            String[] tagsForCategory = categoryTags[tagSetIndex];
            if (tagsForCategory.length >= 2) {
                product.setTags(List.of(tagsForCategory[0], tagsForCategory[1]));
            } else {
                product.setTags(List.of(tagsForCategory[0]));
            }

            // Generate a unique product name
            String[][] modelNames = getModelNames(category);
            String[] models = modelNames[random.nextInt(modelNames.length)];
            String name;
            do {
                String model = models[random.nextInt(models.length)];
                String suffix = modelSuffixes[random.nextInt(modelSuffixes.length)];
                String color = colors[random.nextInt(colors.length)];
                name = model + " " + suffix + " (" + color + ")";
            } while (!usedNames.add(name));
            product.setName(name);

            int basePrice = getBasePrice(category);
            int priceVariation = random.nextInt(5000);
            product.setPrice(BigDecimal.valueOf(basePrice + priceVariation));

            product.setSales(500 + random.nextInt(5000));
            product.setStock(20 + random.nextInt(300));
            product.setDescription(brands[brandIndex] + " " + category + "，" + descriptions[random.nextInt(descriptions.length)]);
            product.setCreateTime(new Date(System.currentTimeMillis() - (long) (random.nextInt(365)) * 24 * 60 * 60 * 1000));

            products.add(product);
        }

        return products;
    }

    private String[][] getModelNames(String category) {
        return switch (category) {
            case "phone" -> new String[][]{{"iPhone", "Pura", "Xiaomi", "Galaxy", "Pixel", "Find", "X", "Mate", "GT", "Nord"}};
            case "laptop" -> new String[][]{{"MacBook", "ThinkPad", "Xiaomi", "Gram", "Surface", "ProArt", "ZenBook", " XPS", "Legion", " Pavilion"}};
            case "headphone" -> new String[][]{{"AirPods", "FreeBuds", "Xiaomi", "WH", "Pixel Buds", "Enco", "WF", "QuietComfort", "Tune", "Cloud"}};
            case "tablet" -> new String[][]{{"iPad", "MatePad", "Xiaomi", "Galaxy", "Surface", "Tab", "Pad", "Yoga", "IdeaPad", "Fire"}};
            case "smartwatch" -> new String[][]{{"Apple Watch", "Watch GT", "Xiaomi", "Galaxy Watch", "Pixel Watch", "Watch", "Fitbit", "TicWatch", "Amazfit", "Fossil"}};
            case "camera" -> new String[][]{{"EOS", "X", "Alpha", "Lumix", "Coolpix", "Insta360", "Hero", "PowerShot", "DSC", "RX"}};
            case "monitor" -> new String[][]{{"Studio Display", "ThinkVision", "Xiaomi", "Odyssey", "Surface", "ProDisplay", "Predator", "UltraGear", "AGON", "FlexScan"}};
            case "keyboard" -> new String[][]{{"Magic Keyboard", "ThinkPad", "Xiaomi", "Galaxy", "Surface", "K380", "Corsair", "Razer", "Logitech", "Keychron"}};
            case "mouse" -> new String[][]{{"Magic Mouse", "ThinkPad", "Xiaomi", "Galaxy", "Surface", "MX Master", "Razer", "Logitech", "SteelSeries", "G"}};
            case "speaker" -> new String[][]{{"HomePod", "Sound", "Xiaomi", "Galaxy", "Home", "Echo", "Sonos", "JBL", "Bose", "Marshall"}};
            default -> new String[][]{{"Product"}};
        };
    }

    private int getBasePrice(String category) {
        return switch (category) {
            case "phone" -> 3000;
            case "laptop" -> 5000;
            case "headphone" -> 500;
            case "tablet" -> 2000;
            case "smartwatch" -> 1500;
            case "camera" -> 4000;
            case "monitor" -> 1500;
            case "keyboard" -> 300;
            case "mouse" -> 200;
            case "speaker" -> 800;
            default -> 1000;
        };
    }
}
