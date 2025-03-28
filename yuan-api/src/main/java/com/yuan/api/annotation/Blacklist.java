package com.yuan.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 黑名单
 */
@Target({ElementType.TYPE ,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Blacklist {

    /**
     * 拦截字段的标识
     * @return
     */
    String key() default "default";

    /**
     * 频率限制 每秒生成令牌数
     *
     * @return
     */
    int rageLimit() default 3;

    /**
     * 是否基于IP地址进行限流
     * @return
     */
    boolean useIp() default false;

    /**
     * 是否基于登录用户进行限流
     * @return
     */
    boolean useUser() default false;

}
