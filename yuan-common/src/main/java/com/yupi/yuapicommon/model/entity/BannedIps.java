package com.yupi.yuapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 
     */
    private String ipAddress;

    /**
     * 
     */
    private String reason;

    /**
     * 
     */
    private Date bannedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}