package com.yuan.api.listener;

import com.yuan.api.event.ApiCallEvent;
import com.yuan.api.model.entity.ApiCallHistory;
import com.yuan.api.service.ApiCallHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class ApiCallEventListener {

    @Resource
    private ApiCallHistoryService apiCallHistoryService;

    @Async
    @EventListener
    public void handleApiCallEvent(ApiCallEvent event) {

        if (StringUtils.isBlank(event.getTraceId())){
            return;
        }

        ApiCallHistory apiCallHistory = new ApiCallHistory();
        apiCallHistory.setId(event.getTraceId());
        apiCallHistory.setUserId(event.getUser().getId());
        apiCallHistory.setClientIp(event.getClientIp());
        apiCallHistory.setHttpMethod(event.getInterfaceInfo().getMethod());
        apiCallHistory.setRequestPath(event.getInterfaceInfo().getUrl());
        apiCallHistory.setTimestamp(new Date());
        apiCallHistory.setInterfaceName(event.getInterfaceInfo().getName());
        apiCallHistory.setInterfaceId(event.getInterfaceInfo().getId());
        apiCallHistory.setDuration(event.getTotalTimeMillis());

        apiCallHistory.setRequestHeaders(event.getRequestHeader());
        apiCallHistory.setRequestBody(event.getRequestParam());
        apiCallHistory.setResponseHeaders(event.getResponseHeader());
        apiCallHistory.setResponseBody(event.getResponseData());
        apiCallHistory.setResponseCode(event.getResponseCode());
        apiCallHistory.setSize(event.getResponseSizeKB());

        apiCallHistory.setStatus(String.valueOf(event.getResponseCode() == 0 ? 1 : 0));

        apiCallHistoryService.save(apiCallHistory);
    }
}
