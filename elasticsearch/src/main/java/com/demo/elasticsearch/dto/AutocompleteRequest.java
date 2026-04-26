package com.demo.elasticsearch.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AutocompleteRequest {
    private String keyword;
    private Integer limit = 10;
}