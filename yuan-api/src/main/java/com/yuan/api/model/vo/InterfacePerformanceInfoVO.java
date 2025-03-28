package com.yuan.api.model.vo;

import lombok.Data;

@Data
public class InterfacePerformanceInfoVO {

    private String interfaceName; // 接口id
    private Double avgTime; // 平均耗时
    private Double errorPer; // 错误率
}
