package com.yuan.api.model.dto.feedback;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yuan.api.common.PageRequest;
import com.yuan.api.model.enums.FeedbackStatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class updateFeedbackRequest {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private FeedbackStatusEnum feedbackStatusEnum;
}
