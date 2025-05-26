package com.yuan.api.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户视图（脱敏）
 *
 *
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 积分数
     */
    private Integer points;

    private Integer status;

    /**
     * 是否记录日志
     */
    private Integer loggingEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private LocalDateTime lastLoginTime;

    private static final long serialVersionUID = 1L;
}