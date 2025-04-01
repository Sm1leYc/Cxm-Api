package com.yuan.api.event;

import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import com.yupi.yuapicommon.model.entity.User;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class ApiCallEvent extends ApplicationEvent {

    private String traceId;
    private User user;
    private String clientIp;
    private InterfaceInfo interfaceInfo;
    private long totalTimeMillis;
    private Map<String, String> requestHeader;
    private String requestParam;
    private int responseCode;
    private String responseData;
    private Map<String, String> responseHeader;
    private double responseSizeKB;

    public ApiCallEvent(Object source, String traceId, User user, String clientIp, InterfaceInfo interfaceInfo,
                        long totalTimeMillis, Map<String, String> requestHeader, String requestParam, int responseCode,
                        String responseData, Map<String, String> responseHeader, double responseSizeKB) {
        super(source);
        this.traceId = traceId;
        this.user = user;
        this.clientIp = clientIp;
        this.interfaceInfo = interfaceInfo;
        this.totalTimeMillis = totalTimeMillis;
        this.requestHeader = requestHeader;
        this.requestParam = requestParam;
        this.responseCode = responseCode;
        this.responseData = responseData;
        this.responseHeader = responseHeader;
        this.responseSizeKB = responseSizeKB;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public InterfaceInfo getInterfaceInfo() {
        return interfaceInfo;
    }

    public void setInterfaceInfo(InterfaceInfo interfaceInfo) {
        this.interfaceInfo = interfaceInfo;
    }

    public long getTotalTimeMillis() {
        return totalTimeMillis;
    }

    public void setTotalTimeMillis(long totalTimeMillis) {
        this.totalTimeMillis = totalTimeMillis;
    }

    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(Map<String, String> requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public Map<String, String> getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(Map<String, String> responseHeader) {
        this.responseHeader = responseHeader;
    }

    public double getResponseSizeKB() {
        return responseSizeKB;
    }

    public void setResponseSizeKB(double responseSizeKB) {
        this.responseSizeKB = responseSizeKB;
    }
}
