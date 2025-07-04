package com.yuan.api.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxmapi.api.v20231124.client.YuanApiClient;
import com.cxmapi.api.v20231124.model.request.ApiRequest;
import com.cxmapi.common.exception.YuanapiSdkException;
import com.cxmapi.common.model.ApiResponse;
import com.cxmapi.common.model.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuan.api.common.*;
import com.yuan.api.constant.CommonConstant;
import com.yuan.api.constant.UserConstant;
import com.yuan.api.event.ApiCallEvent;
import com.yupi.yuapicommon.constant.HttpConstant;
import com.yupi.yuapicommon.constant.RedisConstant;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.model.dto.interfaceinfo.*;
import com.yuan.api.utils.GenerateCodeUtils;
import com.yuan.api.common.BaseResponse;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import com.yupi.yuapicommon.model.entity.User;
import com.yuan.api.service.InterfaceInfoService;
import com.yuan.api.service.UserService;
import com.yupi.yuapicommon.model.enums.InterfaceInfoStatusEnum;
import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;
import com.yupi.yuapicommon.service.InnerRedisService;
import com.yuan.api.utils.ResultUtils;
import com.yupi.yuapicommon.utils.NetUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.yuan.api.constant.UserConstant.ADMIN_ROLE;

/**
 * 接口
 *
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
@Tag(name = "InterfaceInfoController")
public class InterfaceInfoController {

    private InterfaceInfoService interfaceInfoService;
    private UserService userService;
    private InnerRedisService innerRedisService;

    private ApplicationEventPublisher eventPublisher;

    @Resource
    public void setInterfaceInfoService(InterfaceInfoService interfaceInfoService) {
        this.interfaceInfoService = interfaceInfoService;
    }

    @Resource
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Resource
    @DubboReference(timeout = 3000, check = false)
    public void setInnerRedisService(InnerRedisService innerRedisService) {
        this.innerRedisService = innerRedisService;
    }

    @Resource
    public void setEventPublisher(ApplicationEventPublisher applicationEventPublisher){
        this.eventPublisher = applicationEventPublisher;
    }


    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        interfaceInfo.setRequestParamsRemark(JSONUtil.toJsonStr(interfaceInfoAddRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(JSONUtil.toJsonStr(interfaceInfoAddRequest.getResponseParamsRemark()));
        interfaceInfo.setInvokeCount(0);
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     */
    @PostMapping("/delete")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅管理员可删除
//        if (!userService.isAdmin(request)) {
//            throw new BusinessException(ErrorCode.NOT_AUTH);
//        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        interfaceInfo.setRequestParamsRemark(JSONUtil.toJsonStr(interfaceInfoUpdateRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(JSONUtil.toJsonStr(interfaceInfoUpdateRequest.getResponseParamsRemark()));
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 删除接口在网关中对应的缓存结果
        boolean deletionFailed = false; // 跟踪删除状态
        try {
            innerRedisService.deleteKeys(RedisConstant.API_PREFIX + oldInterfaceInfo.getUrl() + ":" +  oldInterfaceInfo.getMethod() + ":*");
        } catch (Exception e) {
            deletionFailed = true; // 删除失败
            log.error("删除时出现了异常:{}", e.getMessage());
        }

        boolean result = interfaceInfoService.updateById(interfaceInfo);

        if (deletionFailed) {
            return ResultUtils.success(result, "更新成功，但删除Redis键时出现问题！");
        }

        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);

        if (interfaceInfo == null){
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        User loginUser = userService.getLoginUserPermitNull(request);
        // 普通用户和游客只能查看上线接口
        if (!interfaceInfo.getStatus().equals(InterfaceInfoStatusEnum.ONLINE.getValue())
                && (loginUser == null || UserConstant.DEFAULT_ROLE.equals(loginUser.getUserRole()))){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        InterfaceInfoVO interfaceInfoVO = interfaceInfoService.getInterfaceInfoVO(interfaceInfo);
        String code = GenerateCodeUtils.generateJavaCode(interfaceInfoVO);
        if (StringUtils.isNotBlank(code)){
            interfaceInfoVO.setExampleCode(code);
        }

        return ResultUtils.success(interfaceInfoVO);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取接口列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String name = interfaceInfoQueryRequest.getName();
        interfaceInfoQueryRequest.setSortField("");
//        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 限制爬虫
        if (size > 100) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.orderBy(StringUtils.isNotBlank("invokeCount"),
                sortOrder.equals(CommonConstant.SORT_ORDER_DESC), "invokeCount");
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);

        User loginUser = userService.getLoginUserPermitNull(request);

        // 普通用户和游客只能查看上线接口
        if (loginUser == null || UserConstant.DEFAULT_ROLE.equals(loginUser.getUserRole())){
            List<InterfaceInfo> interfaceInfos = interfaceInfoPage.getRecords().stream().filter(
                    interfaceInfo ->
                            interfaceInfo.getStatus().equals(1)
            ).collect(Collectors.toList());
            interfaceInfoPage.setRecords(interfaceInfos);
            interfaceInfoPage.setTotal(interfaceInfos.size());
        }

        Page<InterfaceInfoVO> interfaceInfoVOPage = interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage);

        return ResultUtils.success(interfaceInfoVOPage);
    }

    /**
     * 分页获取接口列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page/admin")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoAdmin(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("");
//        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderBy(StringUtils.isNotBlank("invokeCount"),
                sortOrder.equals(CommonConstant.SORT_ORDER_DESC), "invokeCount");
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage));
    }

    /**
     * 发布接口
     *
     */
    @PostMapping("/online")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        Long id = interfaceInfoInvokeRequest.getId();
        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 仅本人或管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 下线接口
     *
     */
    @PostMapping("/offline")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                     HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        Long id = idRequest.getId();
        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 仅本人或管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 调用接口
     *
     */
    @PostMapping("/invoke")
    @SentinelResource(blockHandler = "test1",
                        fallback = "test2")
    public BaseResponse<String> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) throws YuanapiSdkException {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String host = interfaceInfoInvokeRequest.getHost();
        String path = interfaceInfoInvokeRequest.getUrl();
        User loginUser = userService.getLoginUser(request);

        if (loginUser.getStatus() == 0){
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "当前操作无效，请稍后再试或者联系管理员！");
        }

        String clientIp = NetUtils.getClientIpAddress(request);

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(interfaceInfoInvokeRequest.getId());
        interfaceInfo.setMethod(interfaceInfoInvokeRequest.getMethod());
        interfaceInfo.setUrl(interfaceInfoInvokeRequest.getUrl());
        interfaceInfo.setName(interfaceInfoInvokeRequest.getName());

        Config config = new Config.Builder()
                .setBaseurl(host)
                .setAccessKey(loginUser.getAccessKey())
                .setSecretKey(loginUser.getSecretKey())
                .setConnectTimeOut(interfaceInfoInvokeRequest.getConnectTimeout())
                .setReadTimeOut(interfaceInfoInvokeRequest.getReadTimeout())
                .setAutoRetry(interfaceInfoInvokeRequest.isAutoRetry())
                .build();

            // 创建一个req对象
            ApiRequest apiRequest = new ApiRequest();

            Map<String, Object> userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
            apiRequest.setMethod(interfaceInfoInvokeRequest.getMethod());
            apiRequest.setPath(path);
            apiRequest.setRequestParams(userRequestParams);
            // 创建一个client对象
            YuanApiClient yuanApiClient = new YuanApiClient(config);

            try {
                ApiResponse result = yuanApiClient.invokeApi(apiRequest);

                // 计算响应时间
                if (stopWatch.isRunning()){
                    stopWatch.stop();
                }
                long totalTimeMillis = stopWatch.getTotalTimeMillis();

                String resultBody = result.getBody();
                // 计算响应数据包大小
                byte[] responseBytes = getResponseBytes(resultBody);
                int responseSize = responseBytes.length;
                double sizeInKB = Math.round(responseSize / 1024.0 * 100.0) / 100.0;

                // 记录API日志
                if (loginUser.getLoggingEnabled() == 1){
                    logApiCallAsync(loginUser, clientIp, interfaceInfo, result, userRequestParams, totalTimeMillis, sizeInKB);
                }

                return ResultUtils.success(result.getBody(), totalTimeMillis, sizeInKB);
            } catch (YuanapiSdkException e) {
                // 异常逻辑 - 记录日志后重新抛出
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
                long totalTimeMillis = stopWatch.getTotalTimeMillis();

                // 创建错误响应对象用于日志记录
                ApiResponse errorResponse = new ApiResponse(ErrorCode.SDK_INVOKE_ERROR.getCode(),
                        e.getMessage(),
                        new HashMap<>(),
                        new HashMap<>());

                // 记录错误日志
                if (loginUser.getLoggingEnabled() == 1) {
                    logApiCallAsync(loginUser, clientIp, interfaceInfo, errorResponse, userRequestParams, totalTimeMillis, 0);
                }

                return ResultUtils.success(e.getMessage(), totalTimeMillis, 0);
            } catch (IOException e){
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
            }
    }

    public BaseResponse<String> test1(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request, BlockException ex){
        return ResultUtils.success("熔断异常");
    }

    public BaseResponse<String> test2(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request, Throwable ex){
        return ResultUtils.success( "降级异常");
    }

    /**
     * 获取平台接口调用总记录
     * @param request
     * @return
     */
    @GetMapping("/apiCount")
    public BaseResponse<Integer> getApiCallCount(HttpServletRequest request) {
        return ResultUtils.success(interfaceInfoService.getTotalInvokeCount());
    }

    // 生成cURL命令行
    @PostMapping("/generateCurl")
    public BaseResponse<String> generateCurlCommand(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        User loginUser = userService.getLoginUser(request);

        String curlCommand = interfaceInfoService.generateCurlCommand(interfaceInfoInvokeRequest, loginUser.getAccessKey(), loginUser.getSecretKey());
        return ResultUtils.success(curlCommand);
    }

    private void logApiCallAsync(User loginUser, String clientIp, InterfaceInfo interfaceInfo,
                                ApiResponse result, Map<String, Object> userRequestParams,
                                long totalTimeMillis, double sizeInKB) {
        Map<String, String> reqHeaders = result.getRequestHeaders();
        Map<String, String> resHeaders = result.getResponseHeaders();
        String resultBody = result.getBody();
        String traceId = resHeaders.getOrDefault(HttpConstant.TRACE_ID_HEADER, UUID.randomUUID().toString());

        int finalCode = determineFinalStatusCode(result, resultBody);

        // 发布事件 异步记录日志
        eventPublisher.publishEvent(new ApiCallEvent(
                this,
                traceId,
                loginUser,
                clientIp,
                interfaceInfo,
                totalTimeMillis,
                reqHeaders,
                JSON.toJSONString(userRequestParams),
                finalCode,
                resultBody,
                resHeaders,
                sizeInKB
        ));

    }

    /**
     * 确定最终状态码
     */
    private int determineFinalStatusCode(ApiResponse result, String resultBody) {
        // 单独处理SDK调用异常
        if (result.getResponseCode() == ErrorCode.SDK_INVOKE_ERROR.getCode()) {
            return result.getResponseCode();
        }

        // 尝试解析响应体中的code字段
        try {
            JsonObject json = JsonParser.parseString(resultBody).getAsJsonObject();
            return json.get("code").getAsInt();
        } catch (Exception e) {
            // 默认返回成功码
            return ErrorCode.SUCCESS.getCode();
        }
    }

    private byte[] getResponseBytes(String result) throws IOException {
        if (result == null) {
            return new byte[0];
        }

        // 将响应对象转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(result);
            oos.flush();
            return baos.toByteArray();
        }
    }


}
