package com.demo.elasticsearch.clientapi.service.compound;

import com.demo.elasticsearch.dto.ProductSearchRequest;
import com.demo.elasticsearch.dto.ProductSearchResponse;

import java.io.IOException;

public interface CompoundQueryService {
    ProductSearchResponse boolQuery(ProductSearchRequest request) throws IOException;

    ProductSearchResponse boostingQuery(ProductSearchRequest request) throws IOException;

    ProductSearchResponse constantScoreQuery(ProductSearchRequest request) throws IOException;

    ProductSearchResponse disMaxQuery(ProductSearchRequest request) throws IOException;
}