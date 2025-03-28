package com.yuan.api.model.dto.apicallhistory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yuan.api.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiCallHistoryQuery extends PageRequest implements Serializable {

    private String userId; // 自增主键，唯一标识每条记录

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp; // API调用时间
    private String httpMethod; // HTTP方法 (GET, POST等)
    private String interfaceName; // 接口名称
    private String clientIp; // 客户端IP地址
    private Integer responseCode; // 响应状态码
    private Long duration; // 请求处理时间 (秒)
    private String status; // 调用状态 (成功/失败)
}
