package com.yuan.api.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.UUID;

/**
 * 记录所有controller调用方法请求日志（排除/interfaceInfo/invoke）
 *
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 生成唯一跟踪ID并存入MDC
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);
        request.setAttribute(TRACE_ID_KEY, traceId);
        request.setAttribute("startTime", System.currentTimeMillis());

        boolean shouldProceed = true;
        try {
            // 记录请求基本信息
            log.info("======= Request Start =======");
            log.info("Request URL: {}", request.getRequestURL());
            log.info("HTTP Method: {}", request.getMethod());
            log.info("Client IP: {}", request.getRemoteAddr());

            // 记录请求参数
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                log.info("Parameter - {}: {}", paramName, request.getParameter(paramName));
            }

            return shouldProceed;
        } finally {
            if (!shouldProceed) {
                MDC.remove(TRACE_ID_KEY); // 返回false时立即清除MDC
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
       
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // 计算处理时间
            long startTime = (Long) request.getAttribute("startTime");
            long executeTime = System.currentTimeMillis() - startTime;

            // 记录响应信息
            log.info("Response Status: {}", response.getStatus());
            log.info("Request Processing Time: {}ms", executeTime);

            if (ex != null) {
                log.error("Exception occurred: {}", ex.getMessage(), ex);
            }

            log.info("======= Request End =======\n");
        } finally {
            MDC.clear(); // 确保最终清除MDC
        }
    }
}