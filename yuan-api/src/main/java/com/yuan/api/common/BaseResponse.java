package com.yuan.api.common;

import java.io.Serializable;

import com.yupi.yuapicommon.common.ErrorCode;
import lombok.Data;

/**
 * 通用返回类
 *
 * @param <T>
 *
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    private Long costTime;

    private double size;

    public BaseResponse(int code, T data, String message, Long costTime) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.costTime = costTime;
    }

    public BaseResponse(int code, T data, String message, Long costTime, double size) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.costTime = costTime;
        this.size = size;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", null);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), null);
    }
}
