package com.yuan.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.api.utils.ResultUtils;
import com.yuan.api.common.BaseResponse;
import com.yuan.api.model.dto.apicallhistory.ApiCallHistoryQuery;
import com.yuan.api.model.dto.apicallhistory.LoggingStatusRequest;
import com.yuan.api.model.entity.ApiCallHistory;
import com.yuan.api.service.ApiCallHistoryService;
import com.yuan.api.service.UserService;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.model.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


@RestController
@RequestMapping("/apiCallHistory")
@Slf4j
@Tag(name = "ApiCallHistoryController")
public class ApiCallHistoryController {

    private final ApiCallHistoryService apiCallHistoryService;

    private final UserService userService;

    public ApiCallHistoryController(ApiCallHistoryService apiCallHistoryService, UserService userService){
        this.apiCallHistoryService = apiCallHistoryService;
        this.userService = userService;
    }

    /**
     * 根据id获取API调用历史记录
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<ApiCallHistory> getApiCallHistoryById(String id) {
        ApiCallHistory callHistoryById = apiCallHistoryService.getById(id);

        return ResultUtils.success(callHistoryById);
    }

    /**
     * 根据id删除API调用历史记录
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/delete")
    public BaseResponse<Boolean> deleteApiCallHistory(String id ,HttpServletRequest request) {
        if (StringUtils.isBlank(id)){
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }

        userService.getLoginUser(request);
        ApiCallHistory callHistoryById = apiCallHistoryService.getById(id);
        if (callHistoryById == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        boolean b = apiCallHistoryService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 分页获取API调用历史
     * @param apiCallHistoryQueryReq
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ApiCallHistory>> listApiCallHistory(@RequestBody ApiCallHistoryQuery apiCallHistoryQueryReq) {

        long current = apiCallHistoryQueryReq.getCurrent();
        long size = apiCallHistoryQueryReq.getPageSize();

        QueryWrapper<ApiCallHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", apiCallHistoryQueryReq.getUserId());

        if (StringUtils.isNotBlank(apiCallHistoryQueryReq.getInterfaceName())) {
            queryWrapper.like("interfaceName", apiCallHistoryQueryReq.getInterfaceName());
        }
        if (StringUtils.isNotBlank(apiCallHistoryQueryReq.getHttpMethod())) {
            queryWrapper.eq("httpMethod", apiCallHistoryQueryReq.getHttpMethod());
        }
        if (StringUtils.isNotBlank(apiCallHistoryQueryReq.getStatus())) {
            queryWrapper.eq("status", apiCallHistoryQueryReq.getStatus());
        }
        queryWrapper.orderByDesc("timestamp");
        Page<ApiCallHistory> page = new Page<>(current, size);

        Page<ApiCallHistory> interfaceInfoPage = apiCallHistoryService.page(page, queryWrapper);;
        return ResultUtils.success(interfaceInfoPage);
    }


    /**
     * 更新用户记录日志状态
     * @param loggingStatusRequest
     * @param request
     * @return
     */
    @PostMapping("/updateLoggingStatus")
    public BaseResponse<Boolean> updateLoggingStatus(@RequestBody LoggingStatusRequest loggingStatusRequest, HttpServletRequest request){
        if (loggingStatusRequest == null){
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }

        User loginUser = userService.getLoginUser(request);
        if (!Objects.equals(loggingStatusRequest.getUserId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.ERROR_FORBIDDEN);
        }

        int i = userService.updateLoggingStatus(loggingStatusRequest);
        return ResultUtils.success(i > 0);
    }
}
