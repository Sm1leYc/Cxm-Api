package com.yuan.api.service.impl.inner;

import com.yuan.api.service.UserInterfaceInfoService;
import com.yupi.yuapicommon.service.InnerUserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import jakarta.annotation.Resource;

/**
 * 内部用户接口信息服务实现类
 *
 *
 */
@DubboService
@Slf4j
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId, Integer requiredPoints, String traceId) {
        log.info("======= {} 执行扣除积分=======", traceId);
        return userInterfaceInfoService.invokeCount(traceId, interfaceInfoId, userId, requiredPoints);
    }

}
