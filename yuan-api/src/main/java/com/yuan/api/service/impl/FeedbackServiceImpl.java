package com.yuan.api.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.api.mapper.BannedIpsMapper;
import com.yuan.api.mapper.FeedbackMapper;
import com.yuan.api.model.entity.BannedIps;
import com.yuan.api.model.entity.Feedback;
import com.yuan.api.service.BannedIpsService;
import com.yuan.api.service.FeedbackService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
* @author apple
* @description 针对表【banned_ips】的数据库操作Service实现
* @createDate 2023-11-08 20:48:20
*/
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback>
    implements FeedbackService {


}




