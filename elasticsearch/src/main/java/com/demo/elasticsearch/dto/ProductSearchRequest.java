package com.demo.elasticsearch.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class ProductSearchRequest {
    private String keyword;
    private String category;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> tags;
    private String sortField = "createTime";
    private String sortOrder = "desc";

    @Min(value = 1, message = "最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "最小为1")
    @Max(value = 100, message = "最大为100")
    private Integer size = 10;

}
