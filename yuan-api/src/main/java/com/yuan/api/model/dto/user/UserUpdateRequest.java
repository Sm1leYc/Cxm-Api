package com.yuan.api.model.dto.user;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用户更新请求
 *
 *
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;


    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 签名 accessKey
     */
    private String accessKey;

    /**
     * 签名 secretKey
     */
    private String secretKey;

    /**
     * 积分数
     */
    private Integer points;

    private Integer status;

    private static final long serialVersionUID = 1L;
}