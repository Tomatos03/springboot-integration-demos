package com.demo.elasticsearch.clientapi.service.fulltext;

import com.demo.elasticsearch.dto.ProductSearchRequest;
import com.demo.elasticsearch.dto.ProductSearchResponse;

import java.io.IOException;

public interface FulltextQueryService {
    ProductSearchResponse matchQuery(ProductSearchRequest request) throws IOException;

    ProductSearchResponse multiMatchQuery(ProductSearchRequest request) throws IOException;

    ProductSearchResponse matchPhraseQuery(ProductSearchRequest request) throws IOException;

    ProductSearchResponse queryStringQuery(ProductSearchRequest request) throws IOException;
}