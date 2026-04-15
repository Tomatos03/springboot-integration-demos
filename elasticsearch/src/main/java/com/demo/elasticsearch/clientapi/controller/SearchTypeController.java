package com.demo.elasticsearch.clientapi.controller;

import com.demo.elasticsearch.dto.ApiResponse;
import com.demo.elasticsearch.dto.ProductSearchRequest;
import com.demo.elasticsearch.dto.ProductSearchResponse;
import com.demo.elasticsearch.clientapi.service.fulltext.FulltextQueryService;
import com.demo.elasticsearch.clientapi.service.termlevel.TermLevelQueryService;
import com.demo.elasticsearch.clientapi.service.compound.CompoundQueryService;
import com.demo.elasticsearch.clientapi.service.special.SpecialQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/es/client/search")
public class SearchTypeController {

    private final FulltextQueryService fulltextQueryService;
    private final TermLevelQueryService termLevelQueryService;
    private final CompoundQueryService compoundQueryService;
    private final SpecialQueryService specialQueryService;

    public SearchTypeController(FulltextQueryService fulltextQueryService,
                            TermLevelQueryService termLevelQueryService,
                            CompoundQueryService compoundQueryService,
                            SpecialQueryService specialQueryService) {
        this.fulltextQueryService = fulltextQueryService;
        this.termLevelQueryService = termLevelQueryService;
        this.compoundQueryService = compoundQueryService;
        this.specialQueryService = specialQueryService;
    }

    @PostMapping("/fulltext/match")
    public ApiResponse<ProductSearchResponse> matchQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(fulltextQueryService.matchQuery(request));
    }

    @PostMapping("/fulltext/multi-match")
    public ApiResponse<ProductSearchResponse> multiMatchQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(fulltextQueryService.multiMatchQuery(request));
    }

    @PostMapping("/fulltext/match-phrase")
    public ApiResponse<ProductSearchResponse> matchPhraseQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(fulltextQueryService.matchPhraseQuery(request));
    }

    @PostMapping("/fulltext/query-string")
    public ApiResponse<ProductSearchResponse> queryStringQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(fulltextQueryService.queryStringQuery(request));
    }

    @GetMapping("/term")
    public ApiResponse<ProductSearchResponse> termQuery(
            @RequestParam String field,
            @RequestParam String value
    ) throws IOException {
        return ApiResponse.success(termLevelQueryService.termQuery(field, value));
    }

    @GetMapping("/terms")
    public ApiResponse<ProductSearchResponse> termsQuery(
            @RequestParam String field,
            @RequestParam List<String> values) throws IOException {
        return ApiResponse.success(termLevelQueryService.termsQuery(field, values));
    }

    @GetMapping("/range")
    public ApiResponse<ProductSearchResponse> rangeQuery(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) throws IOException {
        return ApiResponse.success(termLevelQueryService.rangeQuery(minPrice, maxPrice));
    }

    @GetMapping("/prefix")
    public ApiResponse<ProductSearchResponse> prefixQuery(
            @RequestParam String field,
            @RequestParam String prefix) throws IOException {
        return ApiResponse.success(termLevelQueryService.prefixQuery(field, prefix));
    }

    @GetMapping("/wildcard")
    public ApiResponse<ProductSearchResponse> wildcardQuery(
            @RequestParam String field,
            @RequestParam String wildcard) throws IOException {
        return ApiResponse.success(termLevelQueryService.wildcardQuery(field, wildcard));
    }

    @GetMapping("/fuzzy")
    public ApiResponse<ProductSearchResponse> fuzzyQuery(
            @RequestParam String field,
            @RequestParam String value) throws IOException {
        return ApiResponse.success(termLevelQueryService.fuzzyQuery(field, value));
    }

    @GetMapping("/ids")
    public ApiResponse<ProductSearchResponse> idsQuery(@RequestParam List<String> ids) throws IOException {
        return ApiResponse.success(termLevelQueryService.idsQuery(ids));
    }

    @PostMapping("/bool")
    public ApiResponse<ProductSearchResponse> boolQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(compoundQueryService.boolQuery(request));
    }

    @PostMapping("/boosting")
    public ApiResponse<ProductSearchResponse> boostingQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(compoundQueryService.boostingQuery(request));
    }

    @PostMapping("/constant-score")
    public ApiResponse<ProductSearchResponse> constantScoreQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(compoundQueryService.constantScoreQuery(request));
    }

    @PostMapping("/dis-max")
    public ApiResponse<ProductSearchResponse> disMaxQuery(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(compoundQueryService.disMaxQuery(request));
    }

    @GetMapping("/more-like-this")
    public ApiResponse<ProductSearchResponse> moreLikeThisQuery(
            @RequestParam String field,
            @RequestParam String likeText) throws IOException {
        return ApiResponse.success(specialQueryService.moreLikeThisQuery(field, likeText));
    }

    @GetMapping("/scripted-metric")
    public ApiResponse<ProductSearchResponse> scriptedMetricQuery() throws IOException {
        return ApiResponse.success(specialQueryService.scriptedMetricQuery());
    }
}