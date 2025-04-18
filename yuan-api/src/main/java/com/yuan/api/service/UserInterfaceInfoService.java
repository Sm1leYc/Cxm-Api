package com.yuan.api.service;


public interface UserInterfaceInfoService {


    /**
     * 更新用户调用次数
     * @param interfaceId
     * @param userId
     * @return
     */
    boolean invokeCount(String traceId, long interfaceId, long userId, Integer requiredPoints);
}
