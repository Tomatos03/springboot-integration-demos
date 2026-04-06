package com.example.cloud.order.feign;

import feign.InvocationContext;
import feign.Request;
import feign.Response;
import feign.ResponseInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignResponseInterceptor implements ResponseInterceptor {

    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        Response response = invocationContext.response();
        Request request = response.request();
        Object result = chain.next(invocationContext);
        log.info("Feign response intercepted, method={}, url={}, status={}, result={}",
                request.httpMethod(), request.url(), response.status(), result);
        return result;
    }
}
