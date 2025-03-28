package com.yupi.yuapicommon.common;

/**
 * 错误码
 *
 * @author apple
 */
public enum ErrorCode {
    /**
     * 成功
     */
    SUCCESS(0, "ok"),

    /**
     * 禁止访问
     */
    ERROR_FORBIDDEN(403, "禁止访问"),


    RATE_LIMIT_EXCEEDED(429, "当前api超过流量限制"),

    /**
     * 系统内部异常
     */
    ERROR_INTERNAL_SERVER(500, "系统内部异常"),


    OPERATION_ERROR(501, "操作失败"),
    /**
     * 网关错误或代理错误
     */
    ERROR_BAD_GATEWAY(502, "网关错误或代理错误"),

    NOT_AUTH(503, "无权限"),

    /**
     * API密钥无效
     */
    ERROR_INVALID_API_KEY(1001, "API密钥无效"),

    /**
     * 参数无效（包括值无效和类型错误）
     */
    ERROR_INVALID_PARAMETER(2002, "参数无效（包括值无效和类型错误）"),

    /**
     * 接口服务不可用
     */
    ERROR_SERVICE_UNAVAILABLE(2003, "服务不可用"),

    NOT_LOGIN_ERROR(2003, "未登录"),

//    /**
//     * 账号已封禁
//     */
//    PROHIBITED(40001, "账号已封禁"),

    /**
     * 网络错误或请求超时
     */
    ERROR_NETWORK(3001, "网络错误或请求超时"),

    /**
     * 超过速率限制
     */
    ERROR_RATE_LIMIT(3002, "超过速率限制"),

    /**
     * 上传文件异常
     */
    UPLOAD_ERROR(3003, "上传文件异常"),

    /**
     * 扣除积分异常
     */
    DEDUCE_POINT_ERROR(3004, "扣除积分发生异常"),

    NOT_FOUND_ERROR(3005, "请求数据不存在");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
