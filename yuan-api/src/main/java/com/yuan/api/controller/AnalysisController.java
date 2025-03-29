package com.yuan.api.controller;

import com.yuan.api.annotation.AuthCheck;
import com.yuan.api.common.BaseResponse;
import com.yuan.api.utils.ResultUtils;
import com.yuan.api.constant.UserConstant;
import com.yuan.api.model.entity.ApiCallHistory;
import com.yuan.api.model.vo.ApiCallStatisticsVO;
import com.yuan.api.model.vo.InterfacePerformanceInfoVO;
import com.yuan.api.service.ApiCallHistoryService;
import com.yuan.api.utils.RedisUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析控制器
 *
 *
 */
@Slf4j
@RestController
@RequestMapping("/analysis")
@Tag(name = "AnalysisController")
public class AnalysisController {

    private final ApiCallHistoryService apiCallHistoryService;
    private final RedisUtils redisUtils;

    public AnalysisController(ApiCallHistoryService apiCallHistoryService,  RedisUtils redisUtils){
        this.apiCallHistoryService = apiCallHistoryService;
        this.redisUtils = redisUtils;
    }

    private static final String REDIS_KEY = "api_calls";

    /**
     * 返回接口错误率和耗时
     * @return
     */
    @GetMapping("/performance")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<InterfacePerformanceInfoVO>> listInterfacePerformanceInfo(){
        List<ApiCallHistory> list = apiCallHistoryService.list();

        Map<Long, List<ApiCallHistory>> interfaceInfoIdObjMap = list.stream()
                .collect(Collectors.groupingBy(ApiCallHistory::getInterfaceId));

        List<InterfacePerformanceInfoVO> performanceInfoList = interfaceInfoIdObjMap.entrySet().stream()
                .map(entry -> {
                    List<ApiCallHistory> apiCallHistories = entry.getValue();
                    String interfaceName = apiCallHistories.get(0).getInterfaceName();
                    double avgTime = apiCallHistories.stream().mapToDouble(ApiCallHistory::getDuration).average().orElse(0);
                    long errorCount = apiCallHistories.stream().filter(apiCallHistory -> "0".equals(apiCallHistory.getStatus())).count();
                    double errorPer = (double) errorCount / apiCallHistories.size() * 100;

                    InterfacePerformanceInfoVO vo = new InterfacePerformanceInfoVO();
                    vo.setInterfaceName(interfaceName);
                    vo.setAvgTime(avgTime);
                    vo.setErrorPer(errorPer);

                    return vo;
                })
                .collect(Collectors.toList());

        return ResultUtils.success(performanceInfoList);
    }

    /**
     * 获取平台过去7天内的接口调用趋势
     * @return
     */
    @GetMapping("/weeklyApiCalls")
    public BaseResponse<List<ApiCallStatisticsVO>> getWeeklyApiCalls() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 构造统计结果的列表
        List<ApiCallStatisticsVO> result = new ArrayList<>();

        // 遍历过去7天的日期，从最早日期开始
        for (int i = 6; i >= 0; i--) {
            // 计算日期
            String date = today.minusDays(i).toString();

            // 从 Redis 中获取对应日期的数据
            String count = redisUtils.hmGet(REDIS_KEY, date);

            ApiCallStatisticsVO statisticsVO = new ApiCallStatisticsVO();
            statisticsVO.setTime(date);
            statisticsVO.setCount(count == null ? 0 : Integer.parseInt(count));

            result.add(statisticsVO);
        }

        return ResultUtils.success(result);
    }

}
