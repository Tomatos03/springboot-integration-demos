package com.demo.elasticsearch.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AutocompleteResponse {
    private List<AutocompleteItem> items = new ArrayList<>();
}