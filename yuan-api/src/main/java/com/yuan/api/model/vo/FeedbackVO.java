package com.yuan.api.model.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yuan.api.model.enums.FeedbackStatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class FeedbackVO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userAccount;

    private String contact;

    private String feedbackType;

    private String feedbackContent;


    private FeedbackStatusEnum status;

    private Date createTime;
}
