package com.yuan.api.handler;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.cxmapi.common.exception.YuanapiSdkException;
import com.yuan.api.common.BaseResponse;
import com.yuan.api.utils.ResultUtils;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 捕获角色校验异常
    @ExceptionHandler(NotRoleException.class)
    public BaseResponse<?> handleNotRoleException(NotRoleException e) {
        // 获取触发校验的角色标识
        String role = e.getRole();
        return ResultUtils.error(ErrorCode.FORBIDDEN_ERROR, "无此角色：" + role + "，禁止访问");
    }

    // 同时处理权限不足异常（如果需要）
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> handleNotPermissionException(NotPermissionException e) {
        return ResultUtils.error(ErrorCode.FORBIDDEN_ERROR, "无此权限：" + e.getPermission() + "，禁止访问");
    }

    @ExceptionHandler(YuanapiSdkException.class)
    public BaseResponse<?> yuanapiSdkExceptionHandler(YuanapiSdkException e) {
        log.error("YuanapiSdkException", e);
        return ResultUtils.error(ErrorCode.SDK_INVOKE_ERROR, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException，", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException，", e);
        return ResultUtils.error(ErrorCode.INTERNAL_SERVER_ERROR, "系统内部异常");
    }
}
