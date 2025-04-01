package com.yupi.yuapicommon.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.gson.Gson;
import com.yupi.yuapicommon.model.entity.InterfaceInfo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 接口信息封装视图
 *
 *
 * @TableName product
 */
@Data
public class InterfaceInfoVO implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;
    /**
     * 主机名
     */
    private String host;

    /**
     * 接口地址
     */
    private String url;

    /**
     * sdk request对象
     */
    private String request;

    /**
     * sdk client对象
     */
    private String client;

    /**
     * 接口调用次数
     */
    private Integer invokeCount;

    /**
     * 所需要积分
     */
    private Integer requiredPoints;

    /**
     * sdk 调用方法
     */
    private String clientMethod;


    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 备注类型
     */
    private String remarkType;

    /**
     * 备注内容
     */
    private String remarkContent;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 接口文档url
     */
    private String documentationUrl;



    /**
     * 请求类型
     */
    private String method;

    /**
     * 实例代码
     */
    private String exampleCode;

    /**
     * 接口类型
     */
    private String type;

    private String webserviceUrl;

    private String webserviceMethod;

    /**
     * 创建时间
     */
    private Date createTime;

    // 是否启用缓存（1: 启用, 0: 不启用）
    private boolean cacheEnabled;

    // 缓存持续时间（以秒为单位），适用于启用缓存的接口
    private int cacheDuration;


    /**
     * 请求参数说明
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;

    /**
     * 响应参数说明
     */
    private List<ResponseParamsRemarkVO> responseParamsRemark;

    private String responseExample;

    /**
     * 包装类转对象
     *
     * @param interfaceInfoVO
     * @return
     */
    public static InterfaceInfo voToObj(InterfaceInfoVO interfaceInfoVO) {
        if (interfaceInfoVO == null) {
            return null;
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoVO, interfaceInfo);
        return interfaceInfo;
    }

    /**
     * 对象转包装类
     *
     * @param interfaceInfo
     * @return
     */
    public static InterfaceInfoVO objToVo(InterfaceInfo interfaceInfo) {
        if (interfaceInfo == null) {
            return null;
        }
        InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
        return interfaceInfoVO;
    }

    private static final long serialVersionUID = 1L;
}