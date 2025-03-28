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
public class InterfaceInfoAddRequest implements Serializable {


    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * sdk request对象
     */
    private String request;

    /**
     * sdk client对象
     */
    private String client;

    /**
     * sdk 调用方法
     */
    private String clientMethod;

    /**
     * 备注类型
     */
    private String remarkType;

    /**
     * 备注内容
     */
    private String remarkContent;


    /**
     *
     */
    private String responseRemark;

    /**
     * 请求参数说明
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;
    /**
     * 响应参数说明
     */
    private List<ResponseParamsRemarkVO> responseParamsRemark;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 请求类型
     */
    private String method;

    private String host;

    /**
     * 接口文档url
     */
    private String documentationUrl;

    /**
     * 所需积分
     */
    private int requiredPoints;

    /**
     * 接口类型
     */
    private String type;

    private String webserviceUrl;

    private String webserviceMethod;

    // 是否启用缓存（1: 启用, 0: 不启用）
    private boolean cacheEnabled;

    // 缓存持续时间（以秒为单位），适用于启用缓存的接口
    private int cacheDuration;
}