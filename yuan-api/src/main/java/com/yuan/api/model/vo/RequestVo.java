package com.yuan.api.model.vo;

import lombok.Data;

@Data
public class RequestVo {

    /**
     * id
     */
    private Long id;

    /**
     * 是否必须
     */
    private String isRequired;

    /**
     * 类型
     */
    private String type;

    private String filedName;
    private String value;
}
