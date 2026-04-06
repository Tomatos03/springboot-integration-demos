package com.example.gateway.predicate;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
public class WhitelistPathRoutePredicateFactory extends AbstractRoutePredicateFactory<WhitelistPathRoutePredicateFactory.Config> {
    public WhitelistPathRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new WhitelistPathPredicate(config);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("whiteList");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Setter
    @Getter
    public static class Config {
        private List<String> whiteList;
    }

    private static class WhitelistPathPredicate implements Predicate<ServerWebExchange> {
        private final Config config;

        public WhitelistPathPredicate(Config config) {
            this.config = config;
        }

        @Override
        public boolean test(ServerWebExchange exchange) {
            String path = exchange.getRequest()
                                  .getURI()
                                  .getPath();
            List<String> whiteList = config.getWhiteList();
            if (whiteList == null || whiteList.isEmpty()) {
                log.warn("Whitelist is empty, all paths will be allowed");
                return true;
            }
            return whiteList.stream()
                            .anyMatch(path::startsWith);
        }
    }
}
