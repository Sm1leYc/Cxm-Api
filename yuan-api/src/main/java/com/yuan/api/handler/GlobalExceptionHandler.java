package com.yuan.api.handler;

import com.cxmapi.common.exception.YuanapiSdkException;
import com.yupi.yuapicommon.common.BaseResponse;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.utils.ResultUtils;
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
        return ResultUtils.error(e.errorCode, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.ERROR_INTERNAL_SERVER, "系统内部异常");
    }
}
