package com.yuan.api.constant;

public interface RedisConstant {


    /**
     * 验证码过期TTL
     */
    Long CAPTCHA_TTL = 300L;

    String BLACK_LIST = "blacklist:";

    String INTERFACE_KEY = "interface:info:";

    // 单个接口调用次数
    String INTERFACE_COUNT_KEY = "count:invokeCount:";

    // 7天内 接口调用次数
    String INTERFACE_CALLS_KEY = "count:apiCalls";

    String TRACE_ID_PREFIX = "traceId:";
}
