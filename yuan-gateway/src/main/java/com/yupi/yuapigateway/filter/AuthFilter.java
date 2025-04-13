package com.yupi.yuapigateway.filter;

import com.cxmapi.api.v20231124.utils.HttpUtils;
import com.cxmapi.common.SignUtil;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.constant.HttpConstant;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import com.yupi.yuapicommon.model.entity.User;
import com.yupi.yuapicommon.model.enums.InterfaceInfoStatusEnum;
import com.yupi.yuapicommon.service.InnerInterfaceInfoService;
import com.yupi.yuapicommon.service.InnerUserInterfaceInfoService;
import com.yupi.yuapicommon.service.InnerUserService;
import com.yupi.yuapicommon.utils.NetUtils;
import com.yupi.yuapigateway.utils.RedisUtils;
import com.yupi.yuapigateway.config.IpBlacklistConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.ClusterRules;
import org.apache.dubbo.common.constants.LoadbalanceRules;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static com.yupi.yuapicommon.constant.RedisConstant.API_PREFIX;

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

    @DubboReference(cluster = ClusterRules.FAIL_FAST, loadbalance = LoadbalanceRules.ROUND_ROBIN)
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Resource
    private IpBlacklistConfig ipBlacklistConfig;

    private static final long EXPIRE = 300L;

    private static final String NONCE_PREFIX = "api:nonce:";


    private static final String LOCK_PREFIX = "lock:user_interface:";

    // 获取锁的超时时间(毫秒)
    private static final long lockTimeout = 3000;
    // 锁的过期时间(毫秒)，防止死锁
    private static final long expireTime = 5000;


    @Override
    public GatewayFilter apply(Config config) {

        return new OrderedGatewayFilter((exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String method = Objects.requireNonNull(request.getMethod()).toString();
            String ipAddress = NetUtils.getIpAddress(request);
            String url = request.getURI().toString();
            String param = "";
            if ("GET".equals(method)){
                param = HttpUtils.getParamsAfterAmpersand(url);
            }else {
                String requestBody = exchange.getAttribute("cachedRequestBody");
                if (!Objects.isNull(requestBody)){
                    param = requestBody.toString();
                }
            }

            String traceId = request.getHeaders().getFirst(HttpConstant.TRACE_ID_HEADER);
            if (StringUtils.isBlank(traceId)) {
                traceId = UUID.randomUUID().toString();
            }

            // 记录参数
            String cacheKey = logParam(exchange, param, traceId, method, ipAddress);

            // 检测ak sk 是否合法
            HttpHeaders headers = request.getHeaders();
            String accessKey = headers.getFirst("X-AccessKey");

            // 检测用户是否存在
            User invokeUser = null;
            try {
                invokeUser = innerUserService.getInvokeUser(accessKey);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            if (invokeUser == null) {
                throw new BusinessException(ErrorCode.INVALID_API_KEY);
            }

            // 鉴权
            checkAuthority(headers, invokeUser.getSecretKey(), param, ipAddress);

            //  判断接口是否存在 以及请求方法
            InterfaceInfo interfaceInfo = null;
            try {
                interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(request.getPath().value(), method);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            if (interfaceInfo == null || interfaceInfo.getStatus() == 0) {
                throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            // 普通用户只能调用上线接口
            if (interfaceInfo.getStatus() != InterfaceInfoStatusEnum.ONLINE.getValue()
                    && "user".equals(invokeUser.getUserRole())){
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
            }

            // 是否使用缓存结果
            boolean cacheEnabled = interfaceInfo.isCacheEnabled();
            int cacheDuration = interfaceInfo.getCacheDuration();

            if (cacheEnabled) {
                // 从Redis获取缓存数据
                String cachedResponse = redisUtils.get(cacheKey);
                if (cachedResponse != null) {
                    log.info("{}走了缓存：{}", traceId, cacheKey);

                    // 基于用户和接口
                    String lockKey = LOCK_PREFIX + invokeUser.getId() + ":" + interfaceInfo.getId();

                    try {
                        // 尝试获取分布式锁
                        boolean locked = redisUtils.tryLock(lockKey, lockTimeout, expireTime);
                        if (!locked) {
                            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "系统繁忙");
                        }

                        try {
                            if (!invokeUserInterfaceInfo(invokeUser.getId(), interfaceInfo.getId(),
                                    interfaceInfo.getRequiredPoints(), traceId)) {
                                throw new BusinessException(ErrorCode.DEDUCE_POINT_ERROR,
                                        "接口调用失败，请先检查您的积分是否充足！");
                            }

                            // 返回缓存的响应
                            ServerHttpResponse response = exchange.getResponse();
                            DataBuffer buffer = response.bufferFactory()
                                    .wrap(cachedResponse.getBytes(StandardCharsets.UTF_8));
                            return response.writeWith(Mono.just(buffer));
                        } catch (BusinessException e){
                            // 其他系统异常
                            log.error("处理业务逻辑异常", e);
                            return Mono.error(e);
                        } finally {
                            // 释放锁
                            redisUtils.unlock(lockKey);
                        }
                    } catch (Exception e) {
                        log.error("缓存处理异常", e);
                        throw new BusinessException(ErrorCode.BAD_GATEWAY_ERROR);
                    }
                }
            }


            // 转发调用接口
            return handleResponse(exchange, chain,
                    interfaceInfo.getId(),
                    invokeUser.getId(),
                    interfaceInfo.getRequiredPoints(),
                    traceId,
                    cacheEnabled,
                    cacheDuration,
                    cacheKey);
        }, -2);  // 需要确保装饰器的优先级高于 NettyWriteResponseFilter（默认优先级为 -1）否则原始响应可能已提交到客户端，导致装饰逻辑被跳过
    }


    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,
                                     long interfaceInfoId, long userId, Integer requiredPoints,
                                     String traceId,
                                     boolean cacheEnabled,
                                     int cacheDuration,
                                     String cacheKey) {

        try {
            ServerHttpResponse originalResponse = exchange.getResponse();

            if (originalResponse.getStatusCode() != HttpStatus.OK) {
                return chain.filter(exchange); // 非200状态码直接放行
            }

            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            // 成功调用接口时，扣除积分
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @NotNull
                @Override
                public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(fluxBody.flatMap(dataBuffer -> {
                            // 扣除积分
                            String lockKey =  LOCK_PREFIX + userId + "_" + interfaceInfoId;

                            try {
                                // 尝试获取分布式锁
                                boolean locked = redisUtils.tryLock(lockKey, lockTimeout, expireTime);

                                if (!locked) {
                                    return Mono.error(new BusinessException(ErrorCode.DEDUCE_POINT_ERROR));
                                }

                                try {
                                    // 扣除积分
                                    if (!invokeUserInterfaceInfo(userId, interfaceInfoId, requiredPoints, traceId)) {
                                        return Mono.error(new BusinessException(ErrorCode.DEDUCE_POINT_ERROR, "接口调用失败，请先检查您的积分是否充足！"));
                                    }

                                    // 读取响应数据 释放内存
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);
                                    String data = new String(content, StandardCharsets.UTF_8); //data

                                    if (cacheEnabled){
                                        if (cacheDuration == -1){
                                            redisUtils.set(cacheKey, data);
                                        } else {
                                            redisUtils.set(cacheKey, data, (long) cacheDuration);
                                        }
                                    }
                                    // 包装处理后的响应数据并返回
                                    return Mono.just(bufferFactory.wrap(content));
                                } catch (BusinessException e){
                                    // 其他系统异常
                                    log.error("处理业务逻辑异常", e);
                                    return Mono.error(e);
                                }finally {
                                    // 释放锁
                                    redisUtils.unlock(lockKey);
                                }
                            } catch (Exception e) {
                                log.error("分布式锁处理异常.\n", e);
                                return Mono.error(new BusinessException(ErrorCode.DEDUCE_POINT_ERROR));
                            }
                        }));
                    }
                    return super.writeWith(body);
                }
            };
            // 使用装饰后的响应继续执行过滤器链
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }catch (Exception e) {
            log.error("网关处理异常.\n" + e);
            return chain.filter(exchange);
        }
    }

    private boolean invokeUserInterfaceInfo(long userId, long interfaceInfoId, Integer requiredPoints, String traceId) {
        try {
            log.info("=== {}开始积分调用 ===", traceId);
            boolean invoked = innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId, requiredPoints, traceId);
            log.info("=== {}结束积分调用 ===", traceId);
            return invoked;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DEDUCE_POINT_ERROR);
        }
    }

    // 鉴权(timestamp + nonce)
    private void checkAuthority(HttpHeaders headers, String secretKey, String param,String sourceAddress) throws BusinessException{
        String nonce = headers.getFirst("x-Nonce");
        String timestamp = headers.getFirst("x-Timestamp");
        String sign = headers.getFirst("x-Sign");

        // 请求头中参数必须完整
        if (StringUtils.isAnyBlank(nonce, sign, timestamp)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        /**
         * 1.ip黑名单
         * 请求IP是否在黑名单中
         */
        boolean ipInBlacklist = ipBlacklistConfig.getIps().contains(sourceAddress);
        if (ipInBlacklist){
            log.info("当前ip处于黑名单中，禁止请求。ip:{}", sourceAddress);
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        /*
         * 2.重放验证
         * 判断timestamp时间戳与服务器时间是否操过60s（过期时间根据业务情况设置）,如果超过了就提示签名过期。
         */
        long currentTime = System.currentTimeMillis() / 1000;
        if (StringUtils.isNotBlank(timestamp) && (currentTime - Long.parseLong(timestamp)) >= EXPIRE) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        /*
         * 3.通过redis判断nonce是否被使用过
         */
        if (nonce == null){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        Boolean nonceExists = redisUtils.exists(NONCE_PREFIX + nonce);
        if (nonceExists){
            // 请求重复，拒绝请求
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        } else {
            redisUtils.setWithRandomOffset(NONCE_PREFIX + nonce, nonce, EXPIRE);
        }

        /**
         * 4.判断签名是否一致
         */
        String serverSign = SignUtil.getSign(StringUtils.isBlank(param) ? "" : param, secretKey, nonce, timestamp);
        if (StringUtils.isBlank(sign) || !sign.equals(serverSign)){
            throw new BusinessException(ErrorCode.SIGNATURE_ERROR);
        }
    }

    // 记录参数
    private String logParam(ServerWebExchange exchange, String param, String traceId, String method, String sourceAddress){
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("X-AccessKey");

        String paramHash = DigestUtils.md5Hex(param);
        String cacheKey = API_PREFIX + request.getPath() + ":" + method + ":" + paramHash;

        log.info("---------------------------");
        log.info("| traceId：" + traceId);
        log.info("| 请求ak：" + accessKey);
        log.info("| 请求路径：" + request.getURI().getPath());
        log.info("| 请求方法：" + method);
        log.info("| 请求IP：" + sourceAddress);
        log.info("| 请求参数：" + param);
        log.info("---------------------------");

        return cacheKey;
    }

    public static class Config {

    }

}
