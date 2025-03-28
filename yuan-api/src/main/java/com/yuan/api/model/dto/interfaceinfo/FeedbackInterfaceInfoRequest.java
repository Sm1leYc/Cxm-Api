package com.yuan.api.model.dto.interfaceinfo;

import com.yupi.yuapicommon.model.vo.RequestParamsRemarkVO;
import com.yupi.yuapicommon.model.vo.ResponseParamsRemarkVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 *
 */
@Data
public class FeedbackInterfaceInfoRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    private String contact;

    private String feedbackType;

    private String feedbackContent;
}