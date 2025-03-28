package com.yuan.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {

    int addInvokeCounts(long interfaceId);

    /**
     * 查询所有接口调用的总次数
     * @return 总调用次数
     */
    @Select("SELECT SUM(invokeCount) FROM interface_info")
    int getTotalInvokeCount();

}




