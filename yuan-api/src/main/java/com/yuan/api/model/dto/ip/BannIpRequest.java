package com.yuan.api.model.dto.ip;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yuan.api.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName banned_ips
 */
@Data
public class BannIpRequest extends PageRequest implements Serializable {

    /**
     * 
     */
    private String ipAddress;

    /**
     * 
     */
    private String reason;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}