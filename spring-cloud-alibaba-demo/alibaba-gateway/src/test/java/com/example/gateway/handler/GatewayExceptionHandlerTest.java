package com.example.gateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayExceptionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
        GatewayExceptionHandler handler = new GatewayExceptionHandler(objectMapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/demo").build());

        handler.handle(exchange, new RuntimeException("boom")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        JsonNode body = objectMapper.readTree(exchange.getResponse().getBodyAsString().block());
        assertThat(body.get("code").asInt()).isEqualTo(500);
        assertThat(body.get("message").asText()).isEqualTo("网关服务异常");
    }

    @Test
    void shouldUseResponseStatusExceptionStatusAndReason() throws Exception {
        GatewayExceptionHandler handler = new GatewayExceptionHandler(objectMapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/demo").build());

        handler.handle(exchange, new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数无效")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        JsonNode body = objectMapper.readTree(exchange.getResponse().getBodyAsString().block());
        assertThat(body.get("code").asInt()).isEqualTo(400);
        assertThat(body.get("message").asText()).isEqualTo("参数无效");
    }
}
