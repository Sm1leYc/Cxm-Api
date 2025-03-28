package com.yuan.api.task;

import com.yuan.api.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Map;

@Component
@Slf4j
@RefreshScope
public class RedisCleanupTask {

    @Resource
    private RedisUtils redisUtils;

    private static final String REDIS_KEY = "api_calls";

    /**
     * 定时任务：每天凌晨2点清理 Redis 中关于每天调用次数 超过7天的过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanUpOldRecords() {
        log.info("----------定时清理Redis过期调用次数开始----------");

        // 获取7天前的日期
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        // 获取 Redis 中所有的记录
        Map<String, String> allRecords = redisUtils.hashMapGet(REDIS_KEY);

        // 遍历所有记录，删除早于7天前的数据
        for (String date : allRecords.keySet()) {
            LocalDate recordDate = LocalDate.parse(date);
            if (recordDate.isBefore(sevenDaysAgo)) {
                redisUtils.hmRemove(REDIS_KEY, date); // 删除过期数据
                log.info("Redis删除过期的数据: " + date);
            }
        }

        log.info("----------定时清理Redis过期调用次数结束----------");
    }
}
