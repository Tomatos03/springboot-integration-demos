package com.demo.elasticsearch.clientapi.controller;

import com.demo.elasticsearch.clientapi.service.autocomplete.AutocompleteService;
import com.demo.elasticsearch.dto.ApiResponse;
import com.demo.elasticsearch.dto.AutocompleteRequest;
import com.demo.elasticsearch.dto.AutocompleteResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Validated
@RestController
@RequestMapping("/api/es/client/autocomplete")
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    public AutocompleteController(AutocompleteService autocompleteService) {
        this.autocompleteService = autocompleteService;
    }

    @GetMapping
    public ApiResponse<AutocompleteResponse> autocomplete(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer limit
    ) throws IOException {
        AutocompleteRequest request = new AutocompleteRequest();
        request.setKeyword(keyword);
        request.setLimit(limit);
        return ApiResponse.success(autocompleteService.autocomplete(request));
    }
}