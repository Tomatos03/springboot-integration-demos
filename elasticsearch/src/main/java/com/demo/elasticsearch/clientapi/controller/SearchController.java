package com.demo.elasticsearch.clientapi.controller;

import com.demo.elasticsearch.clientapi.service.aggregation.AggregationQueryService;
import com.demo.elasticsearch.clientapi.service.product.ProductElasticsearchService;
import com.demo.elasticsearch.dto.ApiResponse;
import com.demo.elasticsearch.dto.CategoryBucket;
import com.demo.elasticsearch.dto.ProductSearchRequest;
import com.demo.elasticsearch.dto.ProductSearchResponse;
import com.demo.elasticsearch.dto.TimeBucket;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/es/client/products")
public class SearchController {

    private final ProductElasticsearchService productElasticsearchService;
    private final AggregationQueryService aggregationQueryService;

    public SearchController(ProductElasticsearchService productElasticsearchService,
                            AggregationQueryService aggregationQueryService) {
        this.productElasticsearchService = productElasticsearchService;
        this.aggregationQueryService = aggregationQueryService;
    }

    @PostMapping("/search")
    public ApiResponse<ProductSearchResponse> search(@Valid @RequestBody ProductSearchRequest request) throws IOException {
        return ApiResponse.success(productElasticsearchService.search(request));
    }

    @GetMapping("/agg/category")
    public ApiResponse<List<CategoryBucket>> categoryAgg(@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size)
            throws IOException {
        return ApiResponse.success(aggregationQueryService.aggregateByCategory(size));
    }

    @GetMapping("/agg/month")
    public ApiResponse<List<TimeBucket>> monthAgg() throws IOException {
        return ApiResponse.success(aggregationQueryService.aggregateByMonth());
    }
}
