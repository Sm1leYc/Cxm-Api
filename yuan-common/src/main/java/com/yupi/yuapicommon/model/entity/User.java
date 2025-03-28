package com.yupi.yuapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 *
 *
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 用户登录错误次数
     */
    private Integer loginFailCount;

    /**
     * 签名 accessKey
     */
    private String accessKey;

    /**
     * 签名 secretKey
     */
    private String secretKey;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后签到时间
     */
    private Date lastSignIn;

    /**
     * 积分数
     */
    private Integer points;

    /**
     * 是否记录日志（1记录 0不记录）
     */
    private Integer loggingEnabled;

    /**
     * (1正常 0禁用)
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}