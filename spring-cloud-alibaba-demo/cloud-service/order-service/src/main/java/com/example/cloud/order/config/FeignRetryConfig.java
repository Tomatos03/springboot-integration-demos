package com.example.cloud.order.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryConfig {

    @Bean
    public Retryer retryer() {
        // public Default(long period, long maxPeriod, int maxAttempts)
        return new Retryer.Default(100, 1000, 3);
    }
}
