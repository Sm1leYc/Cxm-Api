package com.yuan.api.constant;

public interface RedisConstant {


    /**
     * 验证码过期TTL
     */
    Long CAPTCHA_TTL = 300L;

    String BLACK_LIST = "blacklist:";
}
