package com.yupi.yuapigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 读取配置文件 黑名单
 */
@Component
@ConfigurationProperties(prefix = "blacklist")
public class IpBlacklistConfig {
    private Set<String> ips = new HashSet<>();
    public Set<String> getIps() {
        return ips;
    }

    public void setIps(Set<String> ips) {
        this.ips = ips;
    }
}