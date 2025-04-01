package com.yupi.yuapigateway.filter;

import com.yupi.yuapicommon.constant.HttpConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String traceId = exchange.getRequest().getHeaders().getFirst(HttpConstant.TRACE_ID_HEADER);
        if (StringUtils.isBlank(traceId)){
            traceId = UUID.randomUUID().toString();
        }

        // traceId添加到请求头
        ServerHttpRequest mutedRequest = exchange.getRequest()
                .mutate()
                .header(HttpConstant.TRACE_ID_HEADER, traceId)
                .build();
        ServerWebExchange mutedExchange = exchange.mutate().request(mutedRequest).build();

        // traceId添加到响应头
        mutedExchange.getResponse().getHeaders().add(HttpConstant.TRACE_ID_HEADER, traceId);

        return chain.filter(mutedExchange);
    }

    @Override
    public int getOrder() {
        return -3;
    }
}
