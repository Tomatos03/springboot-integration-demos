package com.example.cloud.order.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class GlobalFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-TraceId", UUID.randomUUID().toString());
        log.info("[Global] Feign request intercepted, service={}, method={}, url={}, X-TraceId={}",
                template.feignTarget().name(),
                template.request().httpMethod(),
                template.url(),
                template.headers().get("X-TraceId"));
    }
}
