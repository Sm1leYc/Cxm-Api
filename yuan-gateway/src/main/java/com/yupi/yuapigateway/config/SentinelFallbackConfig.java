package com.yupi.yuapigateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.HashMap;
import java.util.Map;

/**
 * sentinel fallback异常信息配置
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SentinelFallbackConfig {

    @PostConstruct
    public void initBlockHandlers() {
        // 设置限流异常处理
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                String msg = null;
                int code = 500;
                if(t instanceof FlowException){//限流异常
                    code = 429;
                    msg = "请求超过速率限制，请稍后再试";
                }else if(t instanceof DegradeException){//熔断异常
                    code = 503;
                    msg = "接口服务不可用,请稍后再试";
                }else if(t instanceof ParamFlowException){ //热点参数限流
                    msg = "热点参数限流";
                }else if(t instanceof SystemBlockException){ //系统规则异常
                    msg = "系统规则(负载/....不满足要求)";
                }else if(t instanceof AuthorityException){ //授权规则异常
                    msg = "授权规则不通过";
                }

                // 可以根据实际需要构建自己的返回内容
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("code", code);
                responseBody.put("data", null);
                responseBody.put("message", msg);

                // 返回JSON格式，或自行修改返回类型
                return ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(responseBody));
            }
        });
    }
}
