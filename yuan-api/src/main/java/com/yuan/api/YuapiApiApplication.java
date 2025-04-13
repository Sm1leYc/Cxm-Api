package com.yuan.api;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 *
 */
// todo 如需开启 Redis，须移除 exclude 中的内容
//    exclude = {RedisAutoConfiguration.class}
@SpringBootApplication
@EnableScheduling
@EnableDubbo
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class YuapiApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuapiApiApplication.class, args);
    }

}
