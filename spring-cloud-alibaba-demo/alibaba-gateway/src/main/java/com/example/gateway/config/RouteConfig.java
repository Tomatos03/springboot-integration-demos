package com.example.gateway.config;

import com.example.gateway.filter.route.RequestTimestampFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/4/5
 */
@Configuration
@RequiredArgsConstructor
public class RouteConfig {
//    private final RequestTimestampFilter requestTimestampFilter;
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                      .route("order-service", r -> r
//                              .path("/order-service/**")
//                              .filters(f -> f.filter(requestTimestampFilter))
//                              .uri("lb://order-service"))
//                      .build();
//    }
}
