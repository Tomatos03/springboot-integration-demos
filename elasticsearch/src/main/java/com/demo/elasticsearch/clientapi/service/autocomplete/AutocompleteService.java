package com.demo.elasticsearch.clientapi.service.autocomplete;

import com.demo.elasticsearch.dto.AutocompleteRequest;
import com.demo.elasticsearch.dto.AutocompleteResponse;

import java.io.IOException;

public interface AutocompleteService {
    AutocompleteResponse autocomplete(AutocompleteRequest request) throws IOException;
}