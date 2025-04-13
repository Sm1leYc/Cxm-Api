package com.yuan.api.service.impl;


import com.yuan.api.constant.RedisConstant;
import com.yuan.api.utils.RedisUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.service.InterfaceInfoService;
import com.yuan.api.service.UserService;
import com.yuan.api.service.UserInterfaceInfoService;
import com.yupi.yuapicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserInterfaceInfoServiceImpl implements UserInterfaceInfoService{

    private final UserService userService;

    private final InterfaceInfoService interfaceInfoService;

    private final RedisUtils redisUtils;

    public UserInterfaceInfoServiceImpl(UserService userService, InterfaceInfoService interfaceInfoService, RedisUtils redisUtils){
        this.userService = userService;
        this.interfaceInfoService = interfaceInfoService;
        this.redisUtils = redisUtils;
    }


    @Override
    public boolean invokeCount(String traceId, long interfaceId, long userId, Integer requiredPoints) {
        if (interfaceId <= 0 || userId <= 0){
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        // 对于同一请求 防止重复扣除积分
        if (redisUtils.exists(RedisConstant.TRACE_ID_PREFIX + traceId)){
            return true;
        }

        // 扣除积分
        boolean deducted = userService.deductPoints(userId, requiredPoints);

        if (deducted){
            redisUtils.setWithRandomOffset(RedisConstant.TRACE_ID_PREFIX + traceId, "", 900L);
            // 更新接口调用次数
            interfaceInfoService.addInvokeCounts(interfaceId);
        }

        return deducted;
    }


}




