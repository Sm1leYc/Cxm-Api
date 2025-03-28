package com.yuan.api.service.impl;

import com.yuan.api.utils.RedisUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.service.InterfaceInfoService;
import com.yuan.api.service.UserService;
import com.yuan.api.service.UserInterfaceInfoService;
import com.yupi.yuapicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class UserInterfaceInfoServiceImpl implements UserInterfaceInfoService{

    private final UserService userService;

    private final InterfaceInfoService interfaceInfoService;

    public UserInterfaceInfoServiceImpl(UserService userService, InterfaceInfoService interfaceInfoService){
        this.userService = userService;
        this.interfaceInfoService = interfaceInfoService;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invokeCount(long interfaceId, long userId, Integer requiredPoints) {
        if (interfaceId <= 0 || userId <= 0){
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }

        // 检查积分数量是否足够
        if (!userService.hasEnoughPoints(userId, requiredPoints)){
            return false;
        }

        // 更新接口调用次数
        interfaceInfoService.addInvokeCounts(interfaceId);

        // 扣除积分
        boolean deductedPoints = userService.deductPoints(userId, requiredPoints);

        return deductedPoints;
    }


}




