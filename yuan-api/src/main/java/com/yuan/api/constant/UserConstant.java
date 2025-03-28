package com.yuan.api.constant;

/**
 * 用户常量
 *
 *
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    int DEFAULT_POINTS = 200;

    int MAX_POINTS = 2000; // 用户可持有最大积分值

    int ADD_POINTS = 100; // 每次签到获取积分值

}
