package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class Oauth2AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(Oauth2AuthApplication.class, args);
    }
}
