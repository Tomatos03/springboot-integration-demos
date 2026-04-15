package com.demo.elasticsearch.clientapi.service.termlevel;

import com.demo.elasticsearch.dto.ProductSearchResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface TermLevelQueryService {
    ProductSearchResponse termQuery(String field, String value) throws IOException;

    ProductSearchResponse termsQuery(String field, List<String> values) throws IOException;

    ProductSearchResponse rangeQuery(BigDecimal minPrice, BigDecimal maxPrice) throws IOException;

    ProductSearchResponse prefixQuery(String field, String prefix) throws IOException;

    ProductSearchResponse wildcardQuery(String field, String wildcard) throws IOException;

    ProductSearchResponse fuzzyQuery(String field, String value) throws IOException;

    ProductSearchResponse idsQuery(List<String> ids) throws IOException;
}