package com.example.gateway.filter.route;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RequestTimestampGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestTimestampGatewayFilterFactory.Config> {
    public RequestTimestampGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new RequestTimestampFilter();
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("enabled");
    }

    @Setter
    @Getter
    public static class Config {
        private boolean enabled = true;
    }
}