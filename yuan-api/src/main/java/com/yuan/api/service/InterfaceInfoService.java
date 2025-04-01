package com.yuan.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.api.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import com.yupi.yuapicommon.model.vo.InterfaceInfoVO;

import java.util.List;

/**
* @author apple
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-07-22 15:06:21
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {


    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo);

    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage);


    void addInvokeCounts(long interfaceId);

    Integer getTotalInvokeCount();

    // 生成cURL命令
    String generateCurlCommand(InterfaceInfoInvokeRequest request, String accessKey, String secretKey);


}
