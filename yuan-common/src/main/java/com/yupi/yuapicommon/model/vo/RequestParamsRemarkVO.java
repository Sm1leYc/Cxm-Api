package com.yupi.yuapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestParamsRemarkVO implements Serializable {

    private static final long serialVersionUID = -6549477882078242340L;
    /**
     * id
     */
    private Long id;
    /**
     * 名称
     */
    private String name;

    /**
     * 是否必须
     */
    private String isRequired;

    /**
     * 类型
     */
    private String type;

    /**
     * 说明
     */
    private String remark;

    /**
     * 默认值
     */
    private String defaultValue;
}
