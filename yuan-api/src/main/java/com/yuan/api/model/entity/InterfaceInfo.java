package com.yuan.api.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口信息
 * @TableName interface_info
 */
@TableName(value ="interface_info")
@Data
public class InterfaceInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
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
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注类型
     */
    private String remarkType;

    /**
     * 备注内容
     */
    private String remarkContent;

    /**
     * 接口文档url
     */
    private String documentationUrl;

    private String type;

    private String webserviceUrl;

    private String webserviceMethod;

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