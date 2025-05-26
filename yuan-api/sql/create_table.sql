# 建表脚本

-- 创建库
create database if not exists api;

-- 切换库
use api;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userName` varchar(256) DEFAULT NULL COMMENT '用户昵称',
    `userAccount` varchar(256) NOT NULL COMMENT '账号',
    `userAvatar` varchar(1024) DEFAULT NULL COMMENT '用户头像',
    `userRole` varchar(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user / admin',
    `userPassword` varchar(512) DEFAULT NULL COMMENT '密码',
    `accessKey` varchar(512) DEFAULT NULL COMMENT 'accessKey',
    `secretKey` varchar(512) DEFAULT NULL COMMENT 'secretKey',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `lastLoginTime` datetime DEFAULT NULL COMMENT '最后一次登录时间',
    `points` int(11) DEFAULT NULL COMMENT '积分',
    `loggingEnabled` tinyint(4) DEFAULT '0' COMMENT '是否记录调用历史（0-关闭 1开启）',
    `status` tinyint(4) DEFAULT NULL COMMENT '状态 0-禁用1-正常',
    `lastSignIn` datetime DEFAULT NULL COMMENT '最后签到时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uni_userAccount` (`userAccount`) USING BTREE
) COMMENT='用户';


-- 接口信息表
DROP TABLE IF EXISTS `interface_info`;
CREATE TABLE `interface_info` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
     `name` varchar(256) NOT NULL COMMENT '名称',
     `description` varchar(256) DEFAULT NULL COMMENT '描述',
     `url` varchar(512) DEFAULT NULL COMMENT '接口地址',
     `requestHeader` text COMMENT '请求头',
     `responseHeader` text COMMENT '响应头',
     `status` int(11) NOT NULL DEFAULT '0' COMMENT '接口状态（0-关闭，1-开启）',
     `method` varchar(256) NOT NULL COMMENT '请求类型',
     `userId` bigint(20) NOT NULL COMMENT '创建人',
     `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删, 1-已删)',
     `requestParamsRemark` varchar(512) DEFAULT NULL,
     `responseParamsRemark` varchar(512) DEFAULT NULL,
     `host` varchar(255) DEFAULT NULL,
     `request` varchar(255) DEFAULT NULL COMMENT 'SDK request对象',
     `client` varchar(255) DEFAULT NULL COMMENT 'SDK client',
     `clientMethod` varchar(255) DEFAULT NULL COMMENT 'SDK 调用方法',
     `invokeCount` int(11) DEFAULT '0' COMMENT '调用次数',
     `requiredPoints` int(11) DEFAULT NULL COMMENT '所需积分',
     `type` varchar(20) DEFAULT NULL COMMENT '接口类型（http soap）',
     `webserviceUrl` varchar(255) DEFAULT NULL,
     `webserviceMethod` varchar(255) DEFAULT NULL,
     `documentationUrl` varchar(255) DEFAULT NULL COMMENT '接口文档URL',
     `cacheEnabled` int(11) NOT NULL DEFAULT '0' COMMENT '是否启用缓存（1: 启用, 0: 不启用）',
     `cacheDuration` INT COMMENT '缓存持续时间（以秒为单位），适用于启用缓存的接口',
     PRIMARY KEY (`id`) USING BTREE
) COMMENT='接口信息';


-- API调用历史表
DROP TABLE IF EXISTS `api_call_history`;
CREATE TABLE `api_call_history` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `timestamp` datetime NOT NULL COMMENT 'API调用时间',
    `interfaceName` varchar(30) NOT NULL COMMENT '被调用的API名称',
    `requestPath` varchar(50) DEFAULT NULL COMMENT '请求路径',
    `httpMethod` varchar(10) NOT NULL COMMENT 'HTTP方法 (GET, POST等)',
    `requestHeaders` text COMMENT '请求头信息',
    `requestBody` text COMMENT '请求体内容',
    `responseHeaders` text COMMENT '响应头',
    `responseCode` int(11) NOT NULL COMMENT '响应状态码',
    `responseBody` text COMMENT '响应体内容',
    `clientIp` varchar(45) DEFAULT NULL COMMENT '客户端IP地址',
    `userId` bigint(20) DEFAULT NULL COMMENT '用户ID',
    `duration` float DEFAULT NULL COMMENT '请求处理时间 (秒)',
    `status` varchar(10) DEFAULT NULL COMMENT '调用状态 (成功/失败)',
    `interfaceId` bigint(20) DEFAULT NULL COMMENT '接口id',
    PRIMARY KEY (`id`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_api_endpoint` (`interfaceName`),
    KEY `idx_user_id` (`userId`),
    KEY `idx_status` (`status`)
) COMMENT='API调用历史';

-- 封禁IP表
DROP TABLE IF EXISTS `banned_ips`;
CREATE TABLE `banned_ips` (
     `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
     `ipAddress` varchar(45) DEFAULT NULL COMMENT '封禁Ip',
     `reason` varchar(255) DEFAULT NULL COMMENT '封禁理由',
     `bannedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '封禁时间',
     `bannedBy` varchar(255) NOT NULL COMMENT '封禁人',
     `expirationDate` datetime DEFAULT NULL COMMENT '失效时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `unique_ip_address` (`ipAddress`)
) COMMENT='封禁IP';

-- 反馈表
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT,
     `interfaceId` bigint(20) NOT NULL COMMENT '接口id',
     `userId` bigint(20) NOT NULL COMMENT '用户Id',
     `email` varchar(20) DEFAULT NULL COMMENT '邮箱',
     `suggestion` text COMMENT '建议',
     `problem` text COMMENT '问题',
     `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `status` enum('Pending','In Progress','Resolved','Ignored') DEFAULT 'Pending' COMMENT '状态',
     PRIMARY KEY (`id`)
) COMMENT='反馈';



