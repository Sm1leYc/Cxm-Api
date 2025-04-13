package com.yuan.api.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.api.mapper.ApiCallHistoryMapper;
import com.yuan.api.mapper.UserMapper;
import com.yuan.api.model.dto.apicallhistory.LoggingStatusRequest;
import com.yuan.api.model.entity.ApiCallHistory;
import com.yuan.api.service.ApiCallHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ApiCallHistoryServiceImpl extends ServiceImpl<ApiCallHistoryMapper, ApiCallHistory> implements ApiCallHistoryService{

}
