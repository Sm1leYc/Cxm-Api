package com.yuan.api.model.vo;

import com.yuan.api.model.entity.ApiCallHistory;
import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;


@Data
public class ApiCallHistoryVO {

    private Long id; // 唯一标识每条记录
    private Date timestamp; // API调用时间
    private String interfaceName; // 被调用的API名称
    private String httpMethod; // HTTP方法 (GET, POST等)
    private String requestPath; // 请求路径
    private String requestHeaders; // 请求头信息
    private String requestBody; // 请求体内容
    private int responseCode; // 响应状态码
    private String responseBody; // 响应体内容
    private String clientIp; // 客户端IP地址
    private long userId; // 用户ID (若有)
    private float duration; // 请求处理时间 (秒)
    private String status; // 调用状态 (成功/失败)


    public static ApiCallHistoryVO objToVo(ApiCallHistory apiCallHistory){
        if (apiCallHistory == null) {
            return null;
        }
        ApiCallHistoryVO apiCallHistoryVO = new ApiCallHistoryVO();
        BeanUtils.copyProperties(apiCallHistory, apiCallHistoryVO);
        return apiCallHistoryVO;
    }
}
