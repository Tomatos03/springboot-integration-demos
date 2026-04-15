package com.demo.elasticsearch.clientapi.service.special;

import com.demo.elasticsearch.dto.ProductSearchResponse;

import java.io.IOException;

public interface SpecialQueryService {
    ProductSearchResponse moreLikeThisQuery(String field, String likeText) throws IOException;

    ProductSearchResponse scriptedMetricQuery() throws IOException;
}