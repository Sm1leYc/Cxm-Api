package com.yuan.api.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.api.mapper.BannedIpsMapper;
import com.yuan.api.model.entity.BannedIps;
import com.yuan.api.service.BannedIpsService;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;
import com.yupi.yuapicommon.model.vo.RequestParamsRemarkVO;
import com.yupi.yuapicommon.model.vo.ResponseParamsRemarkVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author apple
* @description 针对表【banned_ips】的数据库操作Service实现
* @createDate 2023-11-08 20:48:20
*/
@Service
public class BannedIpsServiceImpl extends ServiceImpl<BannedIpsMapper, BannedIps>
    implements BannedIpsService {

    private final BannedIpsMapper bannedIpsMapper;

    public BannedIpsServiceImpl(BannedIpsMapper bannedIpsMapper){
        this.bannedIpsMapper = bannedIpsMapper;
    }


    @Override
    public Page<BannedIps> getBannedPage(Page<BannedIps> bannedIpsPage){
        List<BannedIps> bannedIpsList = bannedIpsPage.getRecords();
        Page<BannedIps> bannedIpsPage1 = new Page<>(bannedIpsPage.getCurrent(), bannedIpsPage.getSize(), bannedIpsPage.getTotal());


        bannedIpsPage1.setRecords(bannedIpsList);
        return bannedIpsPage1;
    }

    @Override
    public boolean isIPInBlacklist(String ipAddress) {
        String ip = bannedIpsMapper.selectIp(ipAddress);
        if (StringUtils.isNotBlank(ip)){
            return true;
        }


        return false;
    }
}




