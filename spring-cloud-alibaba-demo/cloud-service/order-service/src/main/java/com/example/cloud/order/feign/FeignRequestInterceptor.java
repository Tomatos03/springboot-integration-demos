package com.example.cloud.order.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String requestId = UUID.randomUUID()
                               .toString();
        template.header("X-Request-Id", requestId);
        template.header("X-From-Service", "order-service");

        log.info("Feign request intercepted, method={}, url={}, requestId={}",
                 template.request()
                         .httpMethod(),
                 template.url(),
                 requestId
        );
    }
}
