package com.yupi.yuapigateway.service;

import com.yupi.yuapicommon.service.InnerRedisService;
import com.yupi.yuapigateway.common.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@Slf4j
@DubboService
public class InnerRedisServiceImpl implements InnerRedisService {

    @Resource
    public RedisUtils redisUtils;

    @Override
    public void deleteKeys(String cacheKey) {
        // 清理旧缓存的逻辑
        log.info("-------缓存删除开始：{} ", cacheKey);
        redisUtils.removePattern(cacheKey);
    }
}
