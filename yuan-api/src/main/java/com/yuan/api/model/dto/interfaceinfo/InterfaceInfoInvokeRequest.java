package com.yuan.api.model.dto.interfaceinfo;

import com.yuan.api.model.vo.RequestVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 接口调用请求
 *
 *
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户请求参数
     */
//    private String userRequestParams;

    private Map<String, Object> userRequestParams;

    private String url;

    private String host;

    private String method;
    private String name;

    private boolean autoRetry;

    private int connectTimeout;
    private int readTimeout;

    private static final long serialVersionUID = 1L;
}