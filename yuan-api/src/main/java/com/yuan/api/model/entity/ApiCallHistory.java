package com.yuan.api.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@TableName(value ="api_call_history")
@Data
public class ApiCallHistory {

    private String id; // 唯一标识每条记录
    private Date timestamp; // API调用时间
    private String httpMethod; // HTTP方法 (GET, POST等)
    private String requestPath; // 请求路径
    private String requestHeaders; // 请求头信息
    private String interfaceName; // 接口名称
    private String requestBody; // 请求体内容
    private String responseHeaders; // 响应头
    private Integer responseCode; // 响应状态码
    private String responseBody; // 响应体内容
    private Double size; // 数据体大小
    private String clientIp; // 客户端IP地址
    private Long userId; // 用户ID (若有)
    private Long interfaceId; // 接口ID
    private Long duration; // 请求处理时间 (ms)
    private String status; // 调用状态 (成功/失败)
}
