package com.demo.elasticsearch.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ProductView {
    private String id;
    private String name;
    private String category;
    private String brand;
    private BigDecimal price;
    private Integer sales;
    private Integer stock;
    private List<String> tags;
    private String description;
    private Date createTime;
    private String highlightedName;
    private String highlightedDescription;
}
