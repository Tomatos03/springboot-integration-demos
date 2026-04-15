package com.demo.elasticsearch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "es")
public class ElasticsearchProperties {
    private String host = "localhost";
    private int port = 9200;
    private String scheme = "http";
    private String username;
    private String password;
    private String index = "products";
}