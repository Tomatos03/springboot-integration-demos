package com.example.gateway.filter.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GatewayTimingFilter implements GlobalFilter {

    private static final String START_TIME_ATTR = "gatewayStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        exchange.getAttributes().put(START_TIME_ATTR, System.nanoTime());

        return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        Long startNano = exchange.getAttribute(START_TIME_ATTR);
                        if (startNano != null) {
                            long durationNanos = System.nanoTime() - startNano;
                            long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
                            double seconds = durationMillis / 1000.0;
                            String formattedSeconds = String.format("%.3f", seconds);
                            
                            log.info("{} {} - 耗时: {}s", 
                                     request.getMethod(), 
                                     request.getPath(), 
                                     formattedSeconds);
                        }
                    }));
    }
}