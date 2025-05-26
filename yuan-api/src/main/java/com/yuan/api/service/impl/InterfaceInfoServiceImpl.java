package com.yuan.api.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxmapi.api.v20231124.utils.HttpUtils;
import com.yuan.api.constant.RedisConstant;
import com.yuan.api.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.yuan.api.utils.RedisUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.model.entity.InterfaceInfo ;
import com.yuan.api.mapper.InterfaceInfoMapper ;
import com.yuan.api.service.InterfaceInfoService;
import com.yupi.yuapicommon.model.entity.User;
import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;
import com.yupi.yuapicommon.model.vo.RequestParamsRemarkVO;
import com.yupi.yuapicommon.model.vo.ResponseParamsRemarkVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author apple
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-07-22 15:06:21
*/
@Service
@Slf4j
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {

    private final InterfaceInfoMapper interfaceInfoMapper;

    private final RedisUtils redisUtils;

    public InterfaceInfoServiceImpl(InterfaceInfoMapper interfaceInfoMapper, RedisUtils redisUtils){
        this.interfaceInfoMapper = interfaceInfoMapper;
        this.redisUtils = redisUtils;
    }


    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        //todo
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "接口名称过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 8192) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "接口描述过长");
        }
    }

    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo) {
        InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);

        // 封装请求参数说明 和 响应参数说明
        List<RequestParamsRemarkVO> requestParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getRequestParamsRemark()), RequestParamsRemarkVO.class);
        List<ResponseParamsRemarkVO> responseParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getResponseParamsRemark()), ResponseParamsRemarkVO.class);

        interfaceInfoVO.setRequestParamsRemark(requestParamsRemarkVOList);
        interfaceInfoVO.setResponseParamsRemark(responseParamsRemarkVOList);
        return interfaceInfoVO;
    }

    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage( Page<InterfaceInfo> interfaceInfoPage) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());

        if (CollectionUtils.isEmpty(interfaceInfoList)){
            return interfaceInfoVOPage;
        }
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);

            // 封装请求参数说明和响应参数说明
            List<RequestParamsRemarkVO> requestParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getRequestParamsRemark()), RequestParamsRemarkVO.class);
            List<ResponseParamsRemarkVO> responseParamsRemarkVOList = JSONUtil.toList(JSONUtil.parseArray(interfaceInfo.getResponseParamsRemark()), ResponseParamsRemarkVO.class);
            interfaceInfoVO.setRequestParamsRemark(requestParamsRemarkVOList);
            interfaceInfoVO.setResponseParamsRemark(responseParamsRemarkVOList);

            return interfaceInfoVO;
        }).collect(Collectors.toList());

        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }


    @Async(value = "poolTaskExecutor")
    @Override
    public void addInvokeCounts(long interfaceId) {
        // 更新单个接口调用次数
        interfaceInfoMapper.addInvokeCounts(interfaceId);

        // 每天调用API调用次数+1
        recordCall();
    }

    @Override
    public Integer getTotalInvokeCount() {
        return interfaceInfoMapper.getTotalInvokeCount();
    }

    /**
     * 记录当天的API调用次数
     */
    public void recordCall() {
        // 获取今天的日期
        String today = LocalDate.now().toString();

        // 自增当天调用次数
        redisUtils.hmIncrement(RedisConstant.INTERFACE_CALLS_KEY, today,
                1);
    }

    @Override
    public String generateCurlCommand(InterfaceInfoInvokeRequest request, String accessKey, String secretKey) {
        String params = HttpUtils.getParam(
                request.getMethod(),
                request.getUserRequestParams());

        Map<String, String> headers = HttpUtils.getHeader(null, params, accessKey, secretKey);

        // 构建cURL命令
        return buildCurlCommand(
                request,
                params,
                accessKey,
                headers.get("X-Nonce"),
                headers.get("X-Timestamp"),
                headers.get("X-Sign")
        );
    }

    private String buildCurlCommand(InterfaceInfoInvokeRequest request, String params,
                                    String accessKey, String nonce, String timestamp, String sign) {
        StringBuilder curl = new StringBuilder();
        String method = request.getMethod().toUpperCase();
        String url = request.getHost() + request.getUrl();

        curl.append("curl -X ").append(method);

        // 处理GET请求的参数
        if ("GET".equalsIgnoreCase(method) && params != null && !params.isEmpty()) {
            url = url + "?" + HttpUtils.buildQueryStringForUrl(request.getUserRequestParams());
        }

        curl.append(" '").append(url).append("' \\\n");
        curl.append("  -H 'X-AccessKey: ").append(accessKey).append("' \\\n");
        curl.append("  -H 'X-Nonce: ").append(nonce).append("' \\\n");
        curl.append("  -H 'X-Timestamp: ").append(timestamp).append("' \\\n");
        curl.append("  -H 'X-Sign: ").append(sign).append("'");

        if (!"GET".equalsIgnoreCase(method) && params != null && !params.isEmpty()) {
            curl.append(" \\\n  -H 'Content-Type: application/json' \\\n");
            curl.append("  -d '").append(JSONUtil.toJsonStr(params)).append("'");
        }

        return curl.toString();
    }
}




