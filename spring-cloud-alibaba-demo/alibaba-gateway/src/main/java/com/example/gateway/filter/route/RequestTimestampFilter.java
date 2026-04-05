package com.example.gateway.filter.route;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
public class RequestTimestampFilter implements GatewayFilter, Ordered {

    private static final String TIMESTAMP_HEADER = "X-Request-Timestamp";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String timestamp = Instant.now().toString();
        
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TIMESTAMP_HEADER, timestamp)
                .build();
        
        log.info("[Route Level] - Added timestamp header: {}", timestamp);
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}