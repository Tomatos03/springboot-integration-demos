package com.example.gateway.filter.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(-1)
@Slf4j
@Component
public class GatewayRequestLogFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("Request: {} {}", request.getMethod(), request.getPath());

        return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        log.info("Response: {} - Status: {}", 
                                 request.getPath(),
                                 exchange.getResponse().getStatusCode());
                    }));
    }
}