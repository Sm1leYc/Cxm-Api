package com.yupi.yuapigateway.filter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

/**
 * 自定义限流拦截器
 */
@Configuration
public class RequestRateLimiter {


    // 基于accessKey限流Bean
    @Primary
    @Bean
    KeyResolver accessKeyResolver() {
        return exchange -> {
            String accessKey = exchange.getRequest().getHeaders().getFirst("x-AccessKey");

            return Mono.just(accessKey);
        };

    }

    // 基于ip限流Bean
    @Bean
    KeyResolver ipKeyResolver() {
        return exchange -> {
            String ipAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

            return Mono.just(ipAddress);
        };

    }

    // 基于path限流Bean
    @Bean
    KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

}
