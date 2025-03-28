package com.yuan.api.aop;

import com.yuan.api.annotation.AuthCheck;
import com.yupi.yuapicommon.exception.BusinessException;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.model.entity.User;
import com.yuan.api.model.enums.UserRoleEnum;
import com.yuan.api.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验 AOP
 *
 *
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByText(mustRole);
            if (mustUserRoleEnum == null) {
                mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            }
            if (mustUserRoleEnum == null){
                throw new BusinessException(ErrorCode.NOT_AUTH);
            }
            String userRole = loginUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NOT_AUTH);
            }
            // 必须有管理员权限
            if (!UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NOT_AUTH);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

