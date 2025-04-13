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

    // ============== http错误码 ==============

    /**
     * 禁止访问
     */
    FORBIDDEN_ERROR(403, "禁止访问"),


    TOO_MANY_REQUEST(429, "请求过于频繁"),

    /**
     * 系统内部异常
     */
    INTERNAL_SERVER_ERROR(500, "系统内部异常"),


    /**
     * 网关错误或代理错误
     */
    BAD_GATEWAY_ERROR(502, "网关错误或代理错误"),

    // ============== 权限错误码 ==============

    NOT_AUTH(503, "无权限"),

    /**
     * API密钥无效
     */
    INVALID_API_KEY(1001, "API密钥无效"),
    NOT_LOGIN_ERROR(1002, "未登录"),
    SIGNATURE_ERROR(1003, "签名认证失败"),
    SERVICE_UNAVAILABLE(1004, "接口服务不可用"),


    // ============== 请求参数错误码 ==============
    INVALID_PARAMETER(2001, "参数无效（包括值无效和类型错误）"),
    NOT_FOUND_ERROR(2002, "请求数据不存在"),

    // ============== 操作异常错误码 ==============

    OPERATION_ERROR(3001, "操作失败"),

    /**
     * 上传文件异常
     */
    UPLOAD_ERROR(3002, "上传文件失败"),

    /**
     * 扣除积分异常
     */
    DEDUCE_POINT_ERROR(3003, "扣除积分失败"),

    // ============== SDK错误码 ==============
    SDK_INVOKE_ERROR(4001, "SDK调用失败"),


    ;

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
