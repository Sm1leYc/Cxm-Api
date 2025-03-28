package com.yuan.api.aop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuan.api.common.BaseResponse;
import com.yuan.api.event.ApiCallEvent;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.yuan.api.service.UserService;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.utils.NetUtils;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import com.yupi.yuapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.UUID;

/**
 * 请求响应日志 AOP
 *
 *
 **/
@Aspect
@Component
@Slf4j
public class LogInterceptor {

    // ThreadLocal缓存
    public static final ThreadLocal<InterfaceInfo> INTERFACE_INFO_CACHE = new ThreadLocal<>();

    public static final ThreadLocal<User> USER_INFO_CACHE = new ThreadLocal<>();

    public static final ThreadLocal<String> TRACE_ID_KEY_CACHE = new ThreadLocal<>();

    @Resource
    private ApplicationEventPublisher eventPublisher;


    /**
     * 执行拦截
     */
    @Around("execution(* com.yuan.api.controller.InterfaceInfoController.invokeInterfaceInfo(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取请求路径
        ServletRequestAttributes requestAttributes =(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ( requestAttributes).getRequest();
        HttpServletResponse httpServletResponse = (requestAttributes).getResponse();

        String clientIp = NetUtils.getClientIpAddress(httpServletRequest);

        String traceId = UUID.randomUUID().toString();
        TRACE_ID_KEY_CACHE.set(traceId);

        // 获取请求参数
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args[0], ", ") + "]";

        User loginUser = null;

        // 输出请求日志
        log.info("[{}] request start : ip={}, params={}", traceId,
                clientIp, reqParam);

        try {
            // 执行原方法
            Object result = point.proceed();

            loginUser = USER_INFO_CACHE.get();
            InterfaceInfo interfaceInfo = INTERFACE_INFO_CACHE.get();

            // 输出响应日志
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            long totalTimeMillis = stopWatch.getTotalTimeMillis();

            InterfaceInfoInvokeRequest interfaceInfoInvokeRequests = (InterfaceInfoInvokeRequest) args[0];

            log.info(" [{}] request end : {}", traceId, result.toString());

            // 创建一个新的BaseResponse对象，将totalTimeMillis赋给costTime字段
            BaseResponse originalResponse = (BaseResponse) result;

            // 计算响应数据包大小
            byte[] responseBytes = getResponseBytes(originalResponse.getData());
            int responseSize = responseBytes.length;
            double sizeInKB = Math.round(responseSize / 1024.0 * 100.0) / 100.0;

            BaseResponse modifiedResponse = new BaseResponse(originalResponse.getCode(), originalResponse.getData(), originalResponse.getMessage(), totalTimeMillis, sizeInKB);

            int outerCode = originalResponse.getCode();
            int finalCode = outerCode;

            String dataString = originalResponse.getData().toString();
            try {
                // 解析内层的 data 字段
                JsonObject parsedData = JsonParser.parseString(dataString).getAsJsonObject();
                int innerCode = parsedData.get("code").getAsInt(); // 获取内层的 code
                finalCode = innerCode;  // 内层 code 解析成功时，覆盖最终的 code
            } catch (Exception e) {

            }

            // 记录API日志
            if (loginUser.getLoggingEnabled() == 1){

                // 获取请求头信息
                StringBuilder requestHeaderBuilder = NetUtils.extractRequestHeader(httpServletRequest);
                StringBuilder responseHeaderBuilder = new StringBuilder();
                if (httpServletResponse != null){
                    responseHeaderBuilder = NetUtils.extractResponseHeader(httpServletResponse);
                }

                // 记录请求参数
                String requestParam = interfaceInfoInvokeRequests.getUserRequestParams().toString();

                // 发布事件 异步记录日志
                eventPublisher.publishEvent(new ApiCallEvent(this, traceId, loginUser, clientIp, interfaceInfo,
                        totalTimeMillis, requestHeaderBuilder.toString(), JSON.toJSONString(requestParam),
                        finalCode, modifiedResponse.getData().toString(), responseHeaderBuilder.toString(), sizeInKB));

            }

            return modifiedResponse;
        } catch (Throwable throwable) {
            // 在抛出异常时记录日志
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            long totalTimeMillis = stopWatch.getTotalTimeMillis();

            // 创建一个错误响应
            BaseResponse errorResponse = new BaseResponse(500, null, throwable.getMessage(), totalTimeMillis);

            InterfaceInfo interfaceInfo = INTERFACE_INFO_CACHE.get();
            loginUser = USER_INFO_CACHE.get();

            // 输出请求日志
            log.info("[{}] request error : ip={}, params={}", traceId,
                    clientIp, reqParam);

            InterfaceInfoInvokeRequest interfaceInfoInvokeRequests = (InterfaceInfoInvokeRequest) args[0];

            Integer loggingEnabled = loginUser.getLoggingEnabled();
            if (loggingEnabled != null && loggingEnabled == 1) {
                // 获取请求头信息并拼接成字符串
                StringBuilder headersStringBuilder = NetUtils.extractRequestHeader(httpServletRequest);
                StringBuilder responseHeaderBuilder = new StringBuilder();
                if (httpServletResponse != null){
                    responseHeaderBuilder = NetUtils.extractResponseHeader(httpServletResponse);
                }
                int code = errorResponse.getCode();

                // 记录请求参数
                String requestParam = interfaceInfoInvokeRequests.getUserRequestParams().toString();

                // 发布事件 异步记录日志
                eventPublisher.publishEvent(new ApiCallEvent(this, traceId,
                        loginUser,
                        clientIp,
                        interfaceInfo,
                        totalTimeMillis,
                        headersStringBuilder.toString(),
                        JSON.toJSONString(requestParam),
                        code,
                        JSON.toJSONString(errorResponse),
                        responseHeaderBuilder.toString(),
                        0));
            }

            return errorResponse; // 返回错误响应
        } finally {
            USER_INFO_CACHE.remove();
            TRACE_ID_KEY_CACHE.remove();
            INTERFACE_INFO_CACHE.remove();
        }

    }

    private byte[] getResponseBytes(Object result) throws IOException {
        if (result == null) {
            return new byte[0];
        }

        // 将响应对象转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(result);
            oos.flush();
            return baos.toByteArray();
        }
    }


}

