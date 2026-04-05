package com.example.gateway.handler;

import com.example.common.vo.ResultVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("GatewayExceptionHandler trigged, Handling gateway exception", ex);
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(ex, status);

        if (status.is5xxServerError()) {
            log.error("Gateway request failed, status={}", status.value(), ex);
        } else {
            log.warn("Gateway request failed, status={}, message={}", status.value(), message);
        }

        byte[] body = serializeResponse(ResultVO.fail(status.value(), message), status, message);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (ex instanceof ResponseStatusException responseStatusException) {
            statusCode = responseStatusException.getStatusCode().value();
        }
        HttpStatus status = HttpStatus.resolve(statusCode);
        return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex, HttpStatus status) {
        if (ex instanceof ResponseStatusException responseStatusException
                && StringUtils.hasText(responseStatusException.getReason())) {
            return responseStatusException.getReason();
        }
        if (status.is5xxServerError()) {
            return "网关服务异常";
        }
        return StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "请求处理失败";
    }

    private byte[] serializeResponse(ResultVO<Void> result, HttpStatus status, String message) {
        try {
            return objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("Failed to serialize gateway error response", jsonProcessingException);
            String fallback = String.format("{\"code\":%d,\"message\":\"%s\"}", status.value(), message);
            return fallback.getBytes();
        }
    }
}
