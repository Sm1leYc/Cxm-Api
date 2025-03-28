package com.yuan.api.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yuan.api.model.enums.FeedbackStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 反馈
 */
@Data
@TableName(value ="feedback")
public class Feedback implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userAccount;

    private String contact;

    private String feedbackType;

    private String feedbackContent;

    @TableField(value = "status")
    @EnumValue
    private FeedbackStatusEnum status;

    private Date createTime;
}
