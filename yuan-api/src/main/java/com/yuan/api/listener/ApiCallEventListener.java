package com.yuan.api.listener;

import com.yuan.api.event.ApiCallEvent;
import com.yuan.api.model.entity.ApiCallHistory;
import com.yuan.api.service.ApiCallHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ApiCallEventListener {

    @Resource
    private ApiCallHistoryService apiCallHistoryService;

    @EventListener
    @Async(value = "poolTaskExecutor")
    public void handleApiCallEvent(ApiCallEvent event) {
        if (StringUtils.isBlank(event.getTraceId())){
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ApiCallHistory apiCallHistory = new ApiCallHistory();
        apiCallHistory.setTraceId(event.getTraceId());
        apiCallHistory.setUserId(event.getUser().getId());
        apiCallHistory.setClientIp(event.getClientIp());
        apiCallHistory.setHttpMethod(event.getInterfaceInfo().getMethod());
        apiCallHistory.setRequestPath(event.getInterfaceInfo().getUrl());
        apiCallHistory.setTimestamp(now);
        apiCallHistory.setInterfaceName(event.getInterfaceInfo().getName());
        apiCallHistory.setInterfaceId(event.getInterfaceInfo().getId());
        apiCallHistory.setDuration(event.getTotalTimeMillis());

        apiCallHistory.setRequestHeaders(buildHeadersString(event.getRequestHeader()));
        apiCallHistory.setRequestBody(event.getRequestParam());
        apiCallHistory.setResponseHeaders(buildHeadersString(event.getResponseHeader()));
        apiCallHistory.setResponseBody(event.getResponseData());
        apiCallHistory.setResponseCode(event.getResponseCode());
        apiCallHistory.setSize(event.getResponseSizeKB());

        apiCallHistory.setStatus(String.valueOf(event.getResponseCode() == 0 ? 1 : 0));

        apiCallHistoryService.save(apiCallHistory);
    }

    private String buildHeadersString(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }
}
