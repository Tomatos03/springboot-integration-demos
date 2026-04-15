package com.demo.elasticsearch.clientapi.controller;

import com.demo.elasticsearch.clientapi.service.product.ProductElasticsearchService;
import com.demo.elasticsearch.dto.ApiResponse;
import com.demo.elasticsearch.dto.BulkUpsertRequest;
import com.demo.elasticsearch.dto.BulkUpsertResponse;
import com.demo.elasticsearch.dto.ProductUpsertRequest;
import com.demo.elasticsearch.model.ProductDocument;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/es/client/products")
public class ProductController {

    private final ProductElasticsearchService productElasticsearchService;

    public ProductController(ProductElasticsearchService productElasticsearchService) {
        this.productElasticsearchService = productElasticsearchService;
    }

    @PostMapping("/{id}")
    public ApiResponse<ProductDocument> upsertProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductUpsertRequest request
    ) throws IOException {
        return ApiResponse.success(productElasticsearchService.upsertProduct(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDocument> getProduct(@PathVariable String id) throws IOException {
        return ApiResponse.success(productElasticsearchService.getProductById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteProduct(@PathVariable String id) throws IOException {
        productElasticsearchService.deleteProduct(id);
        return ApiResponse.success(true);
    }

    @PostMapping("/bulk")
    public ApiResponse<BulkUpsertResponse> bulkUpsert(@Valid @RequestBody BulkUpsertRequest request) throws IOException {
        return ApiResponse.success(productElasticsearchService.bulkUpsert(request));
    }

    @PostMapping("/init")
    public ApiResponse<BulkUpsertResponse> initSampleData() throws IOException {
        return ApiResponse.success(productElasticsearchService.initSampleData());
    }

    @DeleteMapping("/delete")
    public ApiResponse<Boolean> deleteIndex() throws IOException {
        return ApiResponse.success(productElasticsearchService.deleteIndex());
    }

    @GetMapping("/all")
    public ApiResponse<List<ProductDocument>> searchAll() throws IOException {
        return ApiResponse.success(productElasticsearchService.searchAll());
    }
}
