package com.yupi.yuapicommon.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;

/**
 * 网络工具类
 * @author apple
 */
@Slf4j
public class NetUtils {

    // 定义不需要记录的安全相关请求头
    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>();

    static {
        // 初始化敏感头部集合
        SENSITIVE_HEADERS.add("Authorization");
        SENSITIVE_HEADERS.add("Set-Cookie");
        SENSITIVE_HEADERS.add("Cookie");
        SENSITIVE_HEADERS.add("WWW-Authenticate");
        SENSITIVE_HEADERS.add("Proxy-Authenticate");
        SENSITIVE_HEADERS.add("Access-Control-Allow-Credentials");
        SENSITIVE_HEADERS.add("Vary");
    }


    /**
     * 获取客户端 IP 地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = Optional.ofNullable(request.getRemoteAddress())
                    .map(address -> address.getAddress().getHostAddress())
                    .orElse("");
            if (ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        // 本机访问
        if ("localhost".equalsIgnoreCase(ip) || "127.0.0.1".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)){
            // 根据网卡取本机配置的IP
            InetAddress inet;
            try {
                inet = InetAddress.getLocalHost();
                ip = inet.getHostAddress();
            } catch (UnknownHostException e) {
                log.error("获取ip地址时出现异常：{}", e.getMessage());
            }
        }
//         如果查找不到 IP,可以返回 127.0.0.1，可以做一定的处理，但是这里不考虑
         if (ip == null) {
             return "unknown";
         }
        return ip;
    }

    /**
     * 获取客户端真实 IP 地址
     *
     * @param request
     * @return
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
                // 本机访问，尝试获取本机配置的 IP 地址
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        // 多个代理的情况，第一个 IP 为客户端真实 IP，多个 IP 按照 ',' 分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        // 本机访问
        if ("localhost".equalsIgnoreCase(ip) || "127.0.0.1".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)){
            // 根据网卡取本机配置的IP
            InetAddress inet;
            try {
                inet = InetAddress.getLocalHost();
                ip = inet.getHostAddress();
            } catch (UnknownHostException e) {
                log.error("获取ip地址时出现异常：{}", e.getMessage());
            }
        }
//         如果查找不到 IP,可以返回 127.0.0.1，可以做一定的处理，但是这里不考虑
        if (ip == null) {
            return "unknown";
        }
        return ip;
    }


    /**
     * 获取mac地址
     */
    public static String getMacAddress() throws Exception {
        // 取mac地址
        byte[] macAddressBytes = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
        // 下面代码是把mac地址拼装成String
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < macAddressBytes.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            // mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(macAddressBytes[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }
        return sb.toString().trim().toUpperCase();
    }

    // 提取并返回非敏感的请求头信息
    public static StringBuilder extractRequestHeader(HttpServletRequest httpServletRequest) {
        // 获取请求头信息并拼接成字符串
        StringBuilder headersStringBuilder = new StringBuilder();

        // 获取这三个特定的请求头
        String[] headersToExtract = {"user-agent", "content-type", "host"};

        for (String header : headersToExtract) {
            String headerValue = httpServletRequest.getHeader(header);
            if (headerValue != null) {
                headersStringBuilder.append(header).append(": ").append(headerValue).append("\n");
            }
        }

        return headersStringBuilder;
    }

    // 提取并返回非敏感的响应头信息
    public static StringBuilder extractResponseHeader(HttpServletResponse response) {
        StringBuilder headersStringBuilder = new StringBuilder();

        // 获取所有响应头的名称
        Collection<String> headerNames = response.getHeaderNames();
        // 遍历每个响应头并构建字符串，过滤掉敏感头部
        for (String headerName : headerNames) {
            if (!SENSITIVE_HEADERS.contains(headerName)) { // 如果不是敏感头部
                Collection<String> headerValues = response.getHeaders(headerName);
                for (String headerValue : headerValues) {
                    headersStringBuilder.append(headerName)
                            .append(": ")
                            .append(headerValue)
                            .append("\n");
                }
            }
        }

        return headersStringBuilder; // 返回包含所有非敏感响应头信息的 StringBuilder
    }

}
