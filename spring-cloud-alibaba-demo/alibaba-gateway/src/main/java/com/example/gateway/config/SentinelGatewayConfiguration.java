package com.example.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Sentinel 网关限流配置。
 *
 * <p>注意事项：</p>
 * <p>1. 限流规则建议通过 Sentinel Dashboard 配置（可视化操作）</p>
 * <p>2. 本配置类仅用于演示限流降级响应定制</p>
 * <p>3. 若需本地配置限流规则，可通过注册对应 Rule Bean 方式实现</p>
 */
@Configuration
public class SentinelGatewayConfiguration {

    @PostConstruct
    public void init() {
        BlockRequestHandler blockRequestHandler = (exchange, ex) -> {
            String result = "{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\"}";
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(result);
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
