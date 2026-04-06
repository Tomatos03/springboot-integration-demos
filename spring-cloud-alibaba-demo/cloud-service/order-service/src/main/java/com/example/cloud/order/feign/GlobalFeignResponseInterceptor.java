package com.example.cloud.order.feign;

import feign.InvocationContext;
import feign.Request;
import feign.Response;
import feign.ResponseInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalFeignResponseInterceptor implements ResponseInterceptor {

    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        Response response = invocationContext.response();
        Request request = response.request();
        Object result = chain.next(invocationContext);
        log.info("[Global] Feign response intercepted, method={}, url={}, status={}, result={}",
                request.httpMethod(), request.url(), response.status(), result);
        return result;
    }
}
