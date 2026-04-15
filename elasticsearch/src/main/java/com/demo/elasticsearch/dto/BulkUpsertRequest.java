package com.demo.elasticsearch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BulkUpsertRequest {
    @Valid
    @NotEmpty(message = "不能为空")
    private List<ProductUpsertRequest> products;

    public List<ProductUpsertRequest> getProducts() {
        return products;
    }

    public void setProducts(List<ProductUpsertRequest> products) {
        this.products = products;
    }
}
