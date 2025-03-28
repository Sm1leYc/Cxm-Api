package com.yuan.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.api.model.entity.BannedIps;

import java.util.List;

/**
* @author apple
* @description 针对表【banned_ips】的数据库操作Service
* @createDate 2023-11-08 20:48:20
*/
public interface BannedIpsService extends IService<BannedIps> {

    Page<BannedIps> getBannedPage(Page<BannedIps> bannedIpsPage);

    // 判断ipAddress是否存在表中
    boolean isIPInBlacklist(String ipAddress);
}
