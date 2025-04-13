package com.yupi.yuapigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapigateway.common.BaseResponse;
import com.yupi.yuapigateway.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;

/**
 * 错误web异常处理程序
 */
@Configuration
@Slf4j
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        DataBufferFactory bufferFactory = response.bufferFactory();
        BaseResponse error;

        if (ex instanceof BusinessException) {
            error = ResultUtils.error(((BusinessException) ex).getCode(), ex.getMessage());
            log.error("【自定义异常】：{}", ex.getMessage());
        } else if (ex instanceof ClosedChannelException) {
            error = ResultUtils.error(ErrorCode.SERVICE_UNAVAILABLE);
            log.error("【网关连接异常】：{}", ex.getMessage());
        }else {
            error = ResultUtils.error(ErrorCode.BAD_GATEWAY_ERROR);
            log.error("【网关异常】：{}", ex);
        }

        response.setStatusCode(HttpStatus.BAD_GATEWAY);
        try {
            byte[] errorBytes = objectMapper.writeValueAsBytes(error);
            DataBuffer dataBuffer = bufferFactory.wrap(errorBytes);
            return response.writeWith(Mono.just(dataBuffer));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化异常：{}", e.getMessage());
            return Mono.error(e);
        }
    }

}