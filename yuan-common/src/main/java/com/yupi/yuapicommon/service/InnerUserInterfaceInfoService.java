package com.yupi.yuapicommon.service;

/**
 * 内部用户接口信息服务
 *
 *
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计
     *
     * @param interfaceInfoId
     * @param userId
     * @param traceId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId, Integer requiredPoints, String traceId) throws InterruptedException;

}
