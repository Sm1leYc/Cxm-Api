package com.yupi.yuapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口信息
 *
 *
 */
@TableName(value ="interface_info")
@Data
public class InterfaceInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
     * 请求参数
     * [
     *   {"name": "username", "type": "string"}
     * ]
     */
//    private String requestParams;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 备注类型
     */
    private String remarkType;

    /**
     * 备注内容
     */
    private String remarkContent;


    /**
     * 请求参数说明
     */
    private String requestParamsRemark;

    /**
     *
     */
    private String responseExample;

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
     * 响应参数说明
     */
    private String responseParamsRemark;

    /**
     * 主机名
     */
    private String host;

    /**
     * 接口状态（0-关闭，1-开启 2-测试）
     */
    private Integer status;

    /**
     * 接口调用次数
     */
    private Integer invokeCount;

    /**
     * 所需要积分
     */
    private Integer requiredPoints;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 接口文档url
     */
    private String documentationUrl;

    /**
     * 接口类型3
     */
    private String type;

    private String webserviceUrl;

    private String webserviceMethod;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    @TableLogic
    private Integer isDelete;

    // 是否启用缓存（1: 启用, 0: 不启用）
    private boolean cacheEnabled;

    // 缓存持续时间（以秒为单位），适用于启用缓存的接口
    private int cacheDuration;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}