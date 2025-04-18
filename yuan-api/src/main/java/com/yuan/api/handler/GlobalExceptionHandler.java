package com.yuan.api.handler;

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

    @ExceptionHandler(YuanapiSdkException.class)
    public BaseResponse<?> yuanapiSdkExceptionHandler(YuanapiSdkException e) {
        log.error("YuanapiSdkException", e);
        return ResultUtils.error(ErrorCode.SDK_INVOKE_ERROR, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.INTERNAL_SERVER_ERROR, "系统内部异常");
    }
}
