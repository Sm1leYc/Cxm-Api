package com.yuan.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuan.api.model.entity.BannedIps;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface BannedIpsMapper extends BaseMapper<BannedIps> {

    String selectIp(String ipAddress);

}




