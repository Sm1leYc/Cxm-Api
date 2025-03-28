package com.yupi.yuapigateway.filter;

import com.cxmapi.common.SignUtil;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import com.yupi.yuapicommon.model.entity.User;
import com.yupi.yuapicommon.service.InnerInterfaceInfoService;
import com.yupi.yuapicommon.service.InnerUserInterfaceInfoService;
import com.yupi.yuapicommon.service.InnerUserService;
import com.yupi.yuapicommon.utils.NetUtils;
import com.yupi.yuapigateway.common.RedisUtils;
import com.yupi.yuapigateway.config.IpBlacklistConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * API请求鉴权 gateway -> interface
 */
@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    public AuthFilter() {
        super(Config.class);
    }


    @DubboReference
    private InnerUserService innerUserService;

    @Resource
    public RedisUtils redisUtils;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Resource
    private IpBlacklistConfig ipBlacklistConfig;

    private static final long ONE_MINUTES = (long) 60;

    private static final String NONCE_PREFIX = "api:nonce:";

    private static final String API_PREFIX = "api:res:";

    private static final String TRACE_ID_HEADER = "X-Trace-Id";


    @Override
    public GatewayFilter apply(Config config) {

        return new OrderedGatewayFilter((exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String method = Objects.requireNonNull(request.getMethod()).toString();
            String ipAddress = NetUtils.getIpAddress(request);

            String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
            if (StringUtils.isBlank(traceId)) {
                traceId = UUID.randomUUID().toString();
            }

            // 记录参数
            String cacheKey = logParam(request, traceId, method, ipAddress);

            // 检测ak sk 是否合法
            HttpHeaders headers = request.getHeaders();
            String accessKey = headers.getFirst("x-AccessKey");

            // 检测用户是否存在
            User invokeUser = null;
            try {
                invokeUser = innerUserService.getInvokeUser(accessKey);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.ERROR_INTERNAL_SERVER);
            }
            if (invokeUser == null) {
                throw new BusinessException(ErrorCode.ERROR_FORBIDDEN, "禁止访问，请先检查您的ak/sk是否配置正确或稍后再试");
            }

            // 鉴权
            checkAuthority(headers, invokeUser.getSecretKey(), ipAddress);

            //  判断接口是否存在 以及请求方法
            InterfaceInfo interfaceInfo = null;
            try {
                interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(request.getPath().value(), method);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.ERROR_INTERNAL_SERVER);
            }

            if (interfaceInfo == null || interfaceInfo.getStatus() == 0) {
                throw new BusinessException(ErrorCode.ERROR_SERVICE_UNAVAILABLE);
            }

            // 是否使用缓存结果
            boolean cacheEnabled = interfaceInfo.isCacheEnabled();
            int cacheDuration = interfaceInfo.getCacheDuration();

            if (cacheEnabled) {
                // 从Redis获取缓存数据
                String cachedResponse = redisUtils.get(cacheKey);
                if (cachedResponse != null) {
                    log.info("走了缓存：{}", cacheKey);

                    // 接口调用计数与积分校验
                    if (!invokeUserInterfaceInfo(invokeUser.getId(), interfaceInfo.getId(), interfaceInfo.getRequiredPoints())) {
                        throw new BusinessException(ErrorCode.DEDUCE_POINT_ERROR, "接口调用失败，请先检查您的积分是否充足！");
                    }

                    // 返回缓存的响应
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    DataBuffer buffer = response.bufferFactory().wrap(cachedResponse.getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Mono.just(buffer));
                }
            }

//             添加 Trace ID 到请求头中，并转发请求
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(TRACE_ID_HEADER, traceId)
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();


            // 转发调用接口
            return handleResponse(mutatedExchange, chain,interfaceInfo.getId(), invokeUser.getId(), interfaceInfo.getRequiredPoints(),
                    cacheEnabled, cacheDuration, cacheKey);
        }, -2);  // 需要确保装饰器的优先级高于 NettyWriteResponseFilter（默认优先级为 -1）否则原始响应可能已提交到客户端，导致装饰逻辑被跳过
    }


    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,
                                     long interfaceInfoId, long userId, Integer requiredPoints,
                                     boolean cacheEnabled,
                                     int cacheDuration,
                                     String cacheKey) {

        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            HttpStatus statusCode = originalResponse.getStatusCode();

            if(statusCode == HttpStatus.OK){
                // 创建一个自定义的响应装饰器
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            return super.writeWith(fluxBody.flatMap(dataBuffer -> {
                                // 扣除积分
                                if (!invokeUserInterfaceInfo(userId, interfaceInfoId, requiredPoints)) {
                                    return Mono.error(new BusinessException(ErrorCode.DEDUCE_POINT_ERROR, "接口调用失败，请先检查您的积分是否充足！"));
                                }
                                // 读取响应数据并释放内存
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志并输出
                                StringBuilder sb2 = new StringBuilder(200);
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);

                                if (cacheEnabled){
                                    if (cacheDuration == -1){
                                        redisUtils.set(cacheKey, data);
                                    } else {
                                        redisUtils.set(cacheKey, data, (long) cacheDuration);
                                    }
                                }
                                // 包装处理后的响应数据并返回
                                return Mono.just(bufferFactory.wrap(content));
                            }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 使用装饰后的响应继续执行过滤器链
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            // 对于其他状态码，直接继续执行过滤器链
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            log.error("网关处理异常.\n" + e);
            return chain.filter(exchange);
        }
    }

    private boolean invokeUserInterfaceInfo(long userId, long interfaceInfoId, Integer requiredPoints) {
        try {
            log.info("开始积分调用~~~~~~");
            return innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId, requiredPoints);
        } catch (Exception e) {
            log.error("invokeCount error:", e);
            throw new BusinessException(ErrorCode.DEDUCE_POINT_ERROR);
        }
    }

    // 鉴权(timestamp + nonce)
    private void checkAuthority(HttpHeaders headers, String secretKey, String sourceAddress) throws BusinessException{
        String clientType = headers.getFirst("X-ClientType");
        String nonce = headers.getFirst("x-Nonce");
        String timestamp = headers.getFirst("x-Timestamp");
        String sign = headers.getFirst("x-Sign");
        String body = headers.getFirst("x-Body");

        // 请求头中参数必须完整
        if (StringUtils.isAnyBlank(nonce, sign, timestamp)) {
            throw new BusinessException(ErrorCode.ERROR_FORBIDDEN);
        }

        /**
         * 1.ip黑名单
         * 请求IP是否在黑名单中
         */
        boolean ipInBlacklist = ipBlacklistConfig.getIps().contains(sourceAddress);
        if (ipInBlacklist){
            log.info("当前ip处于黑名单中，禁止请求。ip:{}", sourceAddress);
            throw new BusinessException(ErrorCode.ERROR_FORBIDDEN);
        }

        /*
         * 2.重放验证
         * 判断timestamp时间戳与当前时间是否操过60s（过期时间根据业务情况设置）,如果超过了就提示签名过期。
         */
        long currentTime = System.currentTimeMillis() / 1000;
        if (StringUtils.isNotBlank(timestamp) && (currentTime - Long.parseLong(timestamp)) >= ONE_MINUTES) {
            throw new BusinessException(ErrorCode.ERROR_FORBIDDEN);
        }

        /*
         * 3.通过redis判断nonce是否被使用过
         */
        if (nonce == null){
            throw new BusinessException(ErrorCode.ERROR_FORBIDDEN);
        }
        Boolean nonceExists = redisUtils.exists(NONCE_PREFIX + nonce);
        if (nonceExists){
            // 请求重复，拒绝请求
            throw new BusinessException(ErrorCode.ERROR_FORBIDDEN);
        } else {
            redisUtils.set(NONCE_PREFIX + nonce, nonce, 60L);
        }

        try {
            body = URLDecoder.decode(body,"utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("body解码错误！");
        }

        /**
         * 4.判断签名是否一致
         */
        String serverSign = SignUtil.getSign(body == null ? "" : body, secretKey, nonce, timestamp);
        if (StringUtils.isBlank(sign) || !sign.equals(serverSign)){
            throw new BusinessException(ErrorCode.ERROR_INVALID_API_KEY);
        }
    }

    // 记录参数
    private String logParam(ServerHttpRequest request, String traceId, String method, String sourceAddress){
        URI uri = request.getURI();
        HttpHeaders headers = request.getHeaders();
        String body = "";
        //解码，解决中文乱码问题
        body = headers.getFirst("x-Body");
        String accessKey = headers.getFirst("x-accessKey");

        try {
            body = URLDecoder.decode(body,"utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("body解码错误！");
        }

        Object param = "";
        if ("GET".equals(method)){
            param = uri;
        }else {
            param = body;
        }

        String clientType = headers.getFirst("X-ClientType");

        String paramHash = DigestUtils.md5Hex(param.toString());
        String cacheKey = API_PREFIX + request.getPath() + ":" + method + ":" + paramHash;

        log.info("---------------------------");
        log.info("| 请求唯一标识：" + traceId);
        log.info("| 请求ak：" + accessKey);
        log.info("| 请求来源：" + clientType);
        log.info("| 请求路径：" + request.getPath());
        log.info("| 请求方法：" + method);
        log.info("| 请求IP：" + sourceAddress);
        log.info("| 请求参数：" + param);
        log.info("---------------------------");

        return cacheKey;
    }

    public static class Config {

    }

}
