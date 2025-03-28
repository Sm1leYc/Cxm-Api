package com.yupi.yuapicommon.common;

import lombok.Data;

import java.io.Serializable;

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

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, null);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), null);
    }
}
