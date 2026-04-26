package com.demo.elasticsearch.clientapi.service.product;

import com.demo.elasticsearch.dto.BulkUpsertRequest;
import com.demo.elasticsearch.dto.BulkUpsertResponse;
import com.demo.elasticsearch.dto.ProductSearchRequest;
import com.demo.elasticsearch.dto.ProductSearchResponse;
import com.demo.elasticsearch.dto.ProductUpsertRequest;
import com.demo.elasticsearch.dto.ProductView;
import com.demo.elasticsearch.model.ProductDocument;

import java.io.IOException;
import java.util.List;

public interface ProductElasticsearchService {
    boolean createIndexIfNotExist() throws IOException;

    boolean deleteIndex() throws IOException;

    ProductDocument upsertProduct(String id, ProductUpsertRequest request) throws IOException;

    ProductDocument getProductById(String id) throws IOException;

    void deleteProduct(String id) throws IOException;

    BulkUpsertResponse bulkUpsert(BulkUpsertRequest request) throws IOException;

    ProductSearchResponse search(ProductSearchRequest request) throws IOException;

    BulkUpsertResponse initSampleData() throws IOException;

    ProductView toView(ProductDocument productDocument);

    List<ProductDocument> searchAll() throws IOException;
}
