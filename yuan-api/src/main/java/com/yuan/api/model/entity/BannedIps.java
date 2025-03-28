package com.yuan.api.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName banned_ips
 */
@TableName(value ="banned_ips")
@Data
public class BannedIps implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 被封禁ip
     */
    private String ipAddress;

    /**
     * 被封禁理由
     */
    private String reason;

    /**
     * 被封禁时间
     */
    private Date bannedAt;

    /**
     * 执行封禁人
     */
    private String bannedBy;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}