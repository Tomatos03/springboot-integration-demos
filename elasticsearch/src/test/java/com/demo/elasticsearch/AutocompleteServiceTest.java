package com.demo.elasticsearch;

import com.demo.elasticsearch.clientapi.service.autocomplete.AutocompleteService;
import com.demo.elasticsearch.dto.AutocompleteRequest;
import com.demo.elasticsearch.dto.AutocompleteResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AutocompleteServiceTest {

    @Autowired
    private AutocompleteService autocompleteService;

    @Test
    void testAutocomplete_withValidKeyword() throws IOException {
        AutocompleteRequest request = new AutocompleteRequest();
        request.setKeyword("i");
        request.setLimit(10);

        AutocompleteResponse response = autocompleteService.autocomplete(request);

        assertNotNull(response);
        assertNotNull(response.getItems());
    }

    @Test
    void testAutocomplete_withEmptyKeyword() throws IOException {
        AutocompleteRequest request = new AutocompleteRequest();
        request.setKeyword("");
        request.setLimit(10);

        AutocompleteResponse response = autocompleteService.autocomplete(request);

        assertNotNull(response);
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void testAutocomplete_withNullKeyword() throws IOException {
        AutocompleteRequest request = new AutocompleteRequest();
        request.setKeyword(null);
        request.setLimit(10);

        AutocompleteResponse response = autocompleteService.autocomplete(request);

        assertNotNull(response);
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void testAutocomplete_withCustomLimit() throws IOException {
        AutocompleteRequest request = new AutocompleteRequest();
        request.setKeyword("phone");
        request.setLimit(5);

        AutocompleteResponse response = autocompleteService.autocomplete(request);

        assertNotNull(response);
        assertNotNull(response.getItems());
    }
}