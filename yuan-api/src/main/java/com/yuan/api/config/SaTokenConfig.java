package com.yuan.api.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.yupi.yuapicommon.common.ErrorCode;
import com.yupi.yuapicommon.exception.BusinessException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 对 Swagger 相关路径进行权限校验
            SaRouter.match("/swagger-ui/**", "/v3/api-docs/**", () -> {
                StpUtil.checkLogin();
                String role = (String) StpUtil.getSession().get("role");
                if (!("admin".equals(role) || "管理员".equals(role))) {
                    throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权访问 Swagger UI");
                }

            });
        })).addPathPatterns("/**");
    }
}