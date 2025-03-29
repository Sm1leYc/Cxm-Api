package com.yuan.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yuan.api.annotation.AuthCheck;
import com.yuan.api.utils.ResultUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yuan.api.model.dto.ip.BannIpRequest;
import com.yuan.api.model.dto.ip.UnBannIpRequest;
import com.yuan.api.model.entity.BannedIps;
import com.yuan.api.service.BannedIpsService;
import com.yuan.api.common.BaseResponse;
import com.yupi.yuapicommon.common.ErrorCode;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/ip")
@Slf4j
@Tag(name = "IpController")
public class IpController {

    @Resource
    private BannedIpsService bannedIpsService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/ban")
    public BaseResponse<Long> banIps(@RequestBody BannIpRequest bannIpRequest, HttpServletRequest request) {
        if (bannIpRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        BannedIps bannedIps = new BannedIps();
        BeanUtils.copyProperties(bannIpRequest, bannedIps);

        // 前端提交请求默认为admin
        bannedIps.setBannedBy("admin");
        bannedIps.setIpAddress(bannedIps.getIpAddress());
        bannedIps.setReason(bannIpRequest.getReason());
        boolean ipInBlacklist = bannedIpsService.isIPInBlacklist(bannedIps.getIpAddress());
        // 不存在黑名单中
        if (!ipInBlacklist){
            bannedIpsService.saveOrUpdate(bannedIps);
            long newInterfaceInfoId = bannedIps.getId();
            return ResultUtils.success(newInterfaceInfoId);
        } else {
            return ResultUtils.error(ErrorCode.ERROR_FORBIDDEN, "请勿重复添加黑名单");
        }
    }


    /**
     * 移除黑名单
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/unban")
    public BaseResponse<Boolean> unBanIps(@RequestBody UnBannIpRequest unBannIpRequest, HttpServletRequest request) {
        if (unBannIpRequest == null || unBannIpRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        long id = unBannIpRequest.getId();
        // 判断是否存在
        BannedIps bannedIps = bannedIpsService.getById(id);
        if (bannedIps == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean b = bannedIpsService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<BannedIps> getBannedIpById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        BannedIps ip = bannedIpsService.getById(id);
        return ResultUtils.success(ip);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<BannedIps>> listInterfaceInfo(BannIpRequest bannIpRequest) {
        QueryWrapper<BannedIps> queryWrapper = new QueryWrapper<>(new BannedIps());
        List<BannedIps> bannedIps = bannedIpsService.list(queryWrapper);
        return ResultUtils.success(bannedIps);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<BannedIps>> listIpsByPage(BannIpRequest bannIpRequest, HttpServletRequest request) {
        if (bannIpRequest == null) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        BannedIps bannedIps = new BannedIps();
        long current = bannIpRequest.getCurrent();
        long size = bannIpRequest.getPageSize();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.ERROR_INVALID_PARAMETER);
        }
        QueryWrapper<BannedIps> queryWrapper = new QueryWrapper<>(bannedIps);
        Page<BannedIps> interfaceInfoPage = bannedIpsService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(bannedIpsService.getBannedPage(interfaceInfoPage));
    }

}
