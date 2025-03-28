package com.yuan.api.model.dto.ip;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName banned_ips
 */
@Data
public class UnBannIpRequest implements Serializable {

    private Long id;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}