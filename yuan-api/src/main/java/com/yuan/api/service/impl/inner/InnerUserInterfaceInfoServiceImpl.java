package com.yuan.api.service.impl.inner;

import com.yuan.api.service.UserInterfaceInfoService;
import com.yupi.yuapicommon.service.InnerUserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

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
    public boolean invokeCount(long interfaceInfoId, long userId, Integer requiredPoints) {
        log.info("=======接口id:{},用户名:{},执行扣除积分=======", interfaceInfoId, userId);
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId, requiredPoints);
    }

    @Override
    public boolean testApi(String name) {
        log.info("~~~~~~~~~~~~输出api：{}", name);
        return false;
    }
}
