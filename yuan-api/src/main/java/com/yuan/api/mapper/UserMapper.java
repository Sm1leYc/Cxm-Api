package com.yuan.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.yuapicommon.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;



@Mapper
public interface UserMapper extends BaseMapper<User> {

    int selectPoints (@Param("userId")long userId);

    int deductPoints (@Param("userId")long userId, @Param("requiredPoints")int requiredPoints);

    int updateLoggingStatus(@Param("loggingEnabled")boolean loggingEnabled, @Param("userId")long userId);

}




