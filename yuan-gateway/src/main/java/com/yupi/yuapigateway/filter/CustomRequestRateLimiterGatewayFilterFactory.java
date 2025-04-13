package com.yupi.yuapigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapigateway.common.BaseResponse;
import com.yupi.yuapigateway.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 限流过滤器工厂
 */
@Slf4j
@Component
public class CustomRequestRateLimiterGatewayFilterFactory extends RequestRateLimiterGatewayFilterFactory {

    private final RateLimiter defaultRateLimiter;

    private final KeyResolver defaultKeyResolver;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomRequestRateLimiterGatewayFilterFactory(RateLimiter defaultRateLimiter, KeyResolver defaultKeyResolver) {
        super(defaultRateLimiter, defaultKeyResolver);
        this.defaultRateLimiter = defaultRateLimiter;
        this.defaultKeyResolver = defaultKeyResolver;
    }

    @Override
    public GatewayFilter apply(Config config) {
        KeyResolver resolver = getOrDefault(config.getKeyResolver(), defaultKeyResolver);
        RateLimiter<Object> limiter = getOrDefault(config.getRateLimiter(), defaultRateLimiter);
        return (exchange, chain) -> resolver.resolve(exchange).flatMap(key -> {
            // 获取路由 ID
            String routeId = config.getRouteId();
            if (routeId == null) {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                routeId = route.getId();
            }

            // 检查限流
            return limiter.isAllowed(routeId, key).flatMap(response -> {

                // 添加令牌桶响应头信息
                /*for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    exchange.getResponse().getHeaders().add(header.getKey(), header.getValue());
                }*/

                if (response.isAllowed()) {
                    // 放行请求
                    return chain.filter(exchange);
                }

                // 构造自定义异常响应
                ServerHttpResponse httpResponse = exchange.getResponse();
                httpResponse.setStatusCode(config.getStatusCode());
                if (!httpResponse.getHeaders().containsKey("Content-Type")) {
                    httpResponse.getHeaders().add("Content-Type", "application/json");
                }

                // 返回自定义限流结果
                BaseResponse error = ResultUtils.error(ErrorCode.TOO_MANY_REQUEST);
                try {
                    byte[] errorBytes = objectMapper.writeValueAsBytes(error);
                    DataBuffer buffer = httpResponse.bufferFactory().wrap(errorBytes);
                    return httpResponse.writeWith(Mono.just(buffer));
                } catch (JsonProcessingException e) {
                    log.error("JSON序列化异常：{}", e.getMessage());
                    return Mono.error(e);
                }
            });
        });
    }

    private <T> T getOrDefault(T configValue, T defaultValue) {
        return (configValue != null) ? configValue : defaultValue;
    }
}
