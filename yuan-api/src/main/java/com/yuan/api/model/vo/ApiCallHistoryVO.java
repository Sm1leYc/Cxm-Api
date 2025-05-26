package com.yuan.api.model.vo;

import com.yuan.api.model.entity.ApiCallHistory;
import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;


@Data
public class ApiCallHistoryVO {

    private Long id;
    private LocalDateTime timestamp; // API调用时间
    private String interfaceName; // 被调用的API名称
    private String httpMethod; // HTTP方法 (GET, POST等)
    private Long duration; // 请求处理时间 (ms)
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
