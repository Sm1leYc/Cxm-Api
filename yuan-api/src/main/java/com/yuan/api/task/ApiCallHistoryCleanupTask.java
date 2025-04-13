package com.yuan.api.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuan.api.constant.TaskCleanupProperties;
import com.yuan.api.mapper.ApiCallHistoryMapper;
import com.yuan.api.model.entity.ApiCallHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ApiCallHistoryCleanupTask {

    @Resource
    private ApiCallHistoryMapper apiCallHistoryMapper;

    @Resource
    private TaskCleanupProperties taskCleanupProperties;

    // 定时删除 API调用历史表 指定天数前的记录
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
//    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void deleteOldApiCallHistory() {
        if (!taskCleanupProperties.isEnabled()) {
            return;
        }
        log.info("----------定时清理调用历史开始----------");

        int days = taskCleanupProperties.getDays(); // 获取配置的删除天数
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        long startTime = System.currentTimeMillis(); // 记录开始时间

        // 构造删除条件
        QueryWrapper<ApiCallHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("timestamp", cutoffDate);

        // 执行删除操作
        int rowsDeleted = apiCallHistoryMapper.delete(queryWrapper);

        long endTime = System.currentTimeMillis(); // 记录结束时间
        long duration = endTime - startTime;

        log.info("----------定时清理调用历史结束----------");
        log.info("删除数据条数={}，删除条件=timestamp < {}，用时时间={} ms",
                rowsDeleted, cutoffDate, duration);
    }
}
