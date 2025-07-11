package com.demo.entity;

import lombok.Data;

/**
 * @author : Tomatos
 * @date : 2025/7/11
 */
@Data
public class Product {
    private Integer id;
    private String name;
    private Double price;
    private Integer stock;
    private String description;
}

