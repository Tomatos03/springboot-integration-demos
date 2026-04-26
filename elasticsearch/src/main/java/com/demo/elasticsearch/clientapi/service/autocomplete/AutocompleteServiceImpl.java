package com.demo.elasticsearch.clientapi.service.autocomplete;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.demo.elasticsearch.config.ElasticsearchProperties;
import com.demo.elasticsearch.dto.AutocompleteItem;
import com.demo.elasticsearch.dto.AutocompleteRequest;
import com.demo.elasticsearch.dto.AutocompleteResponse;
import com.demo.elasticsearch.exception.BizException;
import com.demo.elasticsearch.model.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AutocompleteServiceImpl implements AutocompleteService {

    private static final String DEFAULT_TYPE = "product";

    private final ElasticsearchClient esClient;
    private final ElasticsearchProperties properties;

    public AutocompleteServiceImpl(ElasticsearchClient esClient,
                                   ElasticsearchProperties properties) {
        this.esClient = esClient;
        this.properties = properties;
    }

    @Override
    public AutocompleteResponse autocomplete(AutocompleteRequest request) throws IOException {
        if (!StringUtils.hasText(request.getKeyword())) {
            return new AutocompleteResponse();
        }

        int limit = request.getLimit() == null ? 10 : request.getLimit();

        SearchResponse<ProductDocument> response = esClient.search(builder -> builder
                        .index(indexName())
                        .size(limit)
                        .query(query -> query.prefix(prefix -> prefix
                                .field("name")
                                .value(request.getKeyword())
                                .caseInsensitive(true)
                        )),
                ProductDocument.class
        );

        List<AutocompleteItem> items = response.hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(Hit::source)
                .filter(doc -> doc.getName() != null)
                .map(doc -> new AutocompleteItem(doc.getName(), DEFAULT_TYPE))
                .distinct()
                .collect(Collectors.toList());

        AutocompleteResponse result = new AutocompleteResponse();
        result.setItems(items);
        return result;
    }

    private String indexName() {
        return properties.getIndex();
    }
}