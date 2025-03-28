DROP DATABASE IF EXISTS `cxm-config`;

CREATE DATABASE  `cxm-config` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `cxm-config`;

CREATE TABLE `config_info` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                               `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                               `group_id` varchar(128) DEFAULT NULL,
                               `content` longtext NOT NULL COMMENT 'content',
                               `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
                               `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                               `src_user` text COMMENT 'source user',
                               `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
                               `app_name` varchar(128) DEFAULT NULL,
                               `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                               `c_desc` varchar(256) DEFAULT NULL,
                               `c_use` varchar(64) DEFAULT NULL,
                               `effect` varchar(64) DEFAULT NULL,
                               `type` varchar(64) DEFAULT NULL,
                               `c_schema` text,
                               `encrypted_data_key` text NOT NULL COMMENT '秘钥',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info';

-- 插入配置文件
INSERT INTO `config_info` (
    `id`,
    `data_id`,
    `group_id`,
    `content`,
    `md5`,
    `gmt_create`,
    `gmt_modified`,
    `src_user`,
    `src_ip`,
    `app_name`,
    `tenant_id`,
    `c_desc`,
    `c_use`,
    `effect`,
    `type`,
    `c_schema`,
    `encrypted_data_key`
)
VALUES
    (
        1,
        'cxm-backend-dev.yml',
        'DEFAULT_GROUP',
        'spring:\n  # 支持 swagger3\n  mvc:\n    pathmatch:\n      matching-strategy: ant_path_matcher\n  # session 配置（失效时间：分钟）\n  session:\n    # todo 取消注释开启分布式 session（须先配置 Redis）\n    store-type: redis\n    timeout: 86400\n  # 数据库配置\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://localhost:3306/yuaip\n    username: root\n    password: 123456\n  # Redis 配置\n  redis:\n    database: 3\n    host: localhost\n    port: 6379\n    timeout: 5000\n    password: 123456\n\n  # 文件上传\n  servlet:\n    multipart:\n      # 大小限制\n      max-file-size: 10MB\n\nserver:\n  address: 0.0.0.0\n  port: 8101\n  servlet:\n    # context-path: /backend\n    # cookie 30 天过期\n    session:\n      cookie:\n        max-age: 2592000\nmybatis-plus:\n  configuration:\n    map-underscore-to-camel-case: false\n    default-enum-type-handler: com.yuan.api.config.FeedbackStatusEnumTypeHandler\n  #    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl\n  global-config:\n    db-config:\n      logic-delete-field: isDelete # 全局逻辑删除的实体字段名\n      logic-delete-value: 1 # 逻辑已删除值（默认为 1）\n      logic-not-delete-value: 0 # 逻辑未删除值（默认为 0）\n\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: \"*\"\n      base-path: /cxmapi/actuator\n  endpoint:\n    health:\n      enabled: true\n      show-details: always\n    metrics:\n      enabled: true\n\n# 腾讯云对象存储\ntencentCOS:\n  secretId: ########\n  secretKey: ########\n  rootSrc: ########\n  bucketAddr: ########\n  bucketName: ########\n\ndubbo:\n  application:\n    name: dubbo-cxm-backend\n  protocol:\n    name: dubbo\n    port: 20880\n  registry:\n    id: nacos-registry\n    # todo 线上修改为服务器内网地址\n    address: nacos://localhost:8848\n\nyuanapi:\n  config:\n    access-key: ########\n    secret-key: ########\n    baseurl: https://gateway.ymcapi.xyz\n\nencryption:\n  salt: \"yuan\"\n\ndefaultAvatarUrl: \"https://ymc-4869-1312786139.cos.ap-beijing.myqcloud.com/images/d2473f92-afb0-44a0-9c5a-0ebe99587fe7.jpeg\"\n\ntask:\n  cleanup:\n    enabled: false       # 是否启用清理任务\n    days: 7             # 删除几天前的数据\n\nspringdoc:\n  api-docs:\n    enabled: true\n    path: /api-docs\n  swagger-ui:\n    path: /swagger-ui.html\n  info:\n    title: Cxm-API\n    description: Cxm-API接口文档\n    version: 1.0.0\n    license:\n      name: Apache 2.0\n      url: https://www.apache.org/licenses/LICENSE-2.0.html\n',
        'ceb90050ccf1ce777d54ca2b4c2e3fa6',
        '2020-05-20 12:00:00',
        '2025-03-28 11:25:54',
        NULL,
        '0:0:0:0:0:0:0:1',
        '',
        '',
        'backend配置',
        'null',
        'null',
        'yaml',
        '',
        ''
    );
INSERT INTO `config_info` (
    `id`,
    `data_id`,
    `group_id`,
    `content`,
    `md5`,
    `gmt_create`,
    `gmt_modified`,
    `src_user`,
    `src_ip`,
    `app_name`,
    `tenant_id`,
    `c_desc`,
    `c_use`,
    `effect`,
    `type`,
    `c_schema`,
    `encrypted_data_key`
)
VALUES
    (
        2,
        'cxm-gateway-dev.yml',
        'DEFAULT_GROUP',
        'server:\n  port: 8089\n\nspring:\n  cloud:\n    gateway:\n      forwarded:\n        enabled: true \n      # default-filters:\n      routes:\n        - id: cxm_api\n          uri: http://localhost:8123\n          predicates:\n            - Path=/api/**\n          filters: \n            - name: RequestRateLimiter # 使用Redis限流器\n              args:\n                key-resolver: \"#{@accessKeyResolver}\" # 限流方式：Bean名称\n                redis-rate-limiter.replenishRate: 3 # 生成令牌速率：个/秒\n                redis-rate-limiter.burstCapacity: 10 # 令牌桶容量\n                redis-rate-limiter.requestedTokens: 1 # 每次消费的Token数量\n            - AuthFilter\n        - id: cxm_backend\n          uri: http://localhost:8101\n          predicates:\n            - Path=/backend/**\n          filters:\n            - StripPrefix=1\n\n\n\n  redis:\n    host: localhost\n    port: 6379\n    database: 4\n    password: 123456\n#          filters:\n#            - AddRequestHeader=yupi, swagger\n#            - AddRequestParameter=name, dog\n#            - name: CircuitBreaker\n#              args:\n#                name: myCircuitBreaker\n#                fallbackUri: forward:/fallback\n#        - id: ingredients-fallback\n#          uri: https://yupi.icu\n#          predicates:\n#            - Path=/fallback\n\n#        - id: after_route\n#          uri: https://yupi.icu\n#          predicates:\n#            - After=2017-01-20T17:42:47.789-07:00[America/Denver]\n#        - id: path_route\n#          uri: https://yupi.icu\n#          predicates:\n#              - Path=/api/**\n#        - id: path_route2\n#          uri: https://baidu.com\n#          predicates:\n#            - Path=/baidu/**\n#        - id: before_route\n#          uri: https://yupi.icu\n#          predicates:\n#            - Before=2017-01-20T17:42:47.789-07:00[America/Denver]\n\n\nlogging:\n level:\n   org:\n     springframework:\n       cloud:\n         gateway: info\n\ndubbo:\n application:\n   name: dubbo-springboot-demo-provider\n protocol:\n   name: dubbo\n   port: -1\n registry:\n   id: nacos-registry\n   address: nacos://localhost:8848\n\n\n\n\n\n\n',
        'f52e347b3fd94e38938884fbd1970408',
        '2020-05-14 14:17:55',
        '2025-03-13 02:22:14',
        NULL,
        '0:0:0:0:0:0:0:1',
        '',
        '',
        '网关模块',
        'null',
        'null',
        'yaml',
        '',
        ''
    );
INSERT INTO `config_info` (
    `id`,
    `data_id`,
    `group_id`,
    `content`,
    `md5`,
    `gmt_create`,
    `gmt_modified`,
    `src_user`,
    `src_ip`,
    `app_name`,
    `tenant_id`,
    `c_desc`,
    `c_use`,
    `effect`,
    `type`,
    `c_schema`,
    `encrypted_data_key`
)
VALUES
    (
        130,
        'cxm-gateway-sentinel-flow',
        'DEFAULT_GROUP',
        '[\n  {\n    \"resource\": \"cxm_backend\",\n    \"count\": 10,\n    \"grade\": 1,\n    \"limitApp\": \"default\",\n    \"strategy\": 0,\n    \"controlBehavior\": 0\n  }\n]',
        '74323db43618a5309f9c3480fbc632c5',
        '2025-02-20 06:44:30',
        '2025-03-23 12:24:41',
        NULL,
        '0:0:0:0:0:0:0:1',
        '',
        '',
        '网关限流规则',
        '',
        '',
        'json',
        '',
        ''
    );
INSERT INTO `config_info` (
    `id`,
    `data_id`,
    `group_id`,
    `content`,
    `md5`,
    `gmt_create`,
    `gmt_modified`,
    `src_user`,
    `src_ip`,
    `app_name`,
    `tenant_id`,
    `c_desc`,
    `c_use`,
    `effect`,
    `type`,
    `c_schema`,
    `encrypted_data_key`
)
VALUES
    (
        131,
        'cxm-gateway-sentinel-degrade',
        'DEFAULT_GROUP',
        '[\n  {\n    \"resource\": \"cxm_api\",          \n    \"grade\": 2,                    \n    \"count\": 5,                     \n    \"timeWindow\": 30,              \n    \"minRequestAmount\": 5,          \n    \"statIntervalMs\": 10000       \n  }\n]',
        '2908b8ae6eeaf9e8868265cc26e45391',
        '2025-02-20 06:45:12',
        '2025-02-20 08:10:55',
        NULL,
        '0:0:0:0:0:0:0:1',
        '',
        '',
        '网关熔断规则',
        '',
        '',
        'json',
        '',
        ''
    );
INSERT INTO `config_info` (
    `id`,
    `data_id`,
    `group_id`,
    `content`,
    `md5`,
    `gmt_create`,
    `gmt_modified`,
    `src_user`,
    `src_ip`,
    `app_name`,
    `tenant_id`,
    `c_desc`,
    `c_use`,
    `effect`,
    `type`,
    `c_schema`,
    `encrypted_data_key`
)
VALUES
    (
        235,
        'cxm-gateway-blacklist.yml',
        'DEFAULT_GROUP',
        'blacklist:\n  ips:\n    - \"192.168.1.100\"',
        'e4e24c45cc4a533fe4414a4bf60b8099',
        '2025-02-20 09:00:44',
        '2025-02-20 09:16:32',
        NULL,
        '0:0:0:0:0:0:0:1',
        '',
        '',
        '网关黑名单',
        '',
        '',
        'yaml',
        '',
        ''
    );


/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_info_aggr   */
/******************************************/
CREATE TABLE `config_info_aggr` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                    `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                    `group_id` varchar(128) NOT NULL COMMENT 'group_id',
                                    `datum_id` varchar(255) NOT NULL COMMENT 'datum_id',
                                    `content` longtext NOT NULL COMMENT '内容',
                                    `gmt_modified` datetime NOT NULL COMMENT '修改时间',
                                    `app_name` varchar(128) DEFAULT NULL,
                                    `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='增加租户字段';


/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_info_beta   */
/******************************************/
CREATE TABLE `config_info_beta` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                    `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                    `group_id` varchar(128) NOT NULL COMMENT 'group_id',
                                    `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
                                    `content` longtext NOT NULL COMMENT 'content',
                                    `beta_ips` varchar(1024) DEFAULT NULL COMMENT 'betaIps',
                                    `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
                                    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                    `src_user` text COMMENT 'source user',
                                    `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
                                    `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                                    `encrypted_data_key` text NOT NULL COMMENT '秘钥',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_beta';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_info_tag   */
/******************************************/
CREATE TABLE `config_info_tag` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                   `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                   `group_id` varchar(128) NOT NULL COMMENT 'group_id',
                                   `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
                                   `tag_id` varchar(128) NOT NULL COMMENT 'tag_id',
                                   `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
                                   `content` longtext NOT NULL COMMENT 'content',
                                   `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
                                   `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                   `src_user` text COMMENT 'source user',
                                   `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_tag';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = config_tags_relation   */
/******************************************/
CREATE TABLE `config_tags_relation` (
                                        `id` bigint(20) NOT NULL COMMENT 'id',
                                        `tag_name` varchar(128) NOT NULL COMMENT 'tag_name',
                                        `tag_type` varchar(64) DEFAULT NULL COMMENT 'tag_type',
                                        `data_id` varchar(255) NOT NULL COMMENT 'data_id',
                                        `group_id` varchar(128) NOT NULL COMMENT 'group_id',
                                        `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
                                        `nid` bigint(20) NOT NULL AUTO_INCREMENT,
                                        PRIMARY KEY (`nid`),
                                        UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
                                        KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_tag_relation';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = group_capacity   */
/******************************************/
CREATE TABLE `group_capacity` (
                                  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `group_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
                                  `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
                                  `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
                                  `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
                                  `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，，0表示使用默认值',
                                  `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
                                  `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
                                  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='集群、各Group容量信息表';

/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = his_config_info   */
/******************************************/
CREATE TABLE `his_config_info` (
                                   `id` bigint(20) unsigned NOT NULL,
                                   `nid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                                   `data_id` varchar(255) NOT NULL,
                                   `group_id` varchar(128) NOT NULL,
                                   `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
                                   `content` longtext NOT NULL,
                                   `md5` varchar(32) DEFAULT NULL,
                                   `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `src_user` text,
                                   `src_ip` varchar(50) DEFAULT NULL,
                                   `op_type` char(10) DEFAULT NULL,
                                   `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
                                   `encrypted_data_key` text NOT NULL COMMENT '秘钥',
                                   PRIMARY KEY (`nid`),
                                   KEY `idx_gmt_create` (`gmt_create`),
                                   KEY `idx_gmt_modified` (`gmt_modified`),
                                   KEY `idx_did` (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='多租户改造';


/******************************************/
/*   数据库全名 = nacos_config   */
/*   表名称 = tenant_capacity   */
/******************************************/
CREATE TABLE `tenant_capacity` (
                                   `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `tenant_id` varchar(128) NOT NULL DEFAULT '' COMMENT 'Tenant ID',
                                   `quota` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
                                   `usage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
                                   `max_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
                                   `max_aggr_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
                                   `max_aggr_size` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
                                   `max_history_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
                                   `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='租户容量信息表';


CREATE TABLE `tenant_info` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                               `kp` varchar(128) NOT NULL COMMENT 'kp',
                               `tenant_id` varchar(128) default '' COMMENT 'tenant_id',
                               `tenant_name` varchar(128) default '' COMMENT 'tenant_name',
                               `tenant_desc` varchar(256) DEFAULT NULL COMMENT 'tenant_desc',
                               `create_source` varchar(32) DEFAULT NULL COMMENT 'create_source',
                               `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
                               `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
                               KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='tenant_info';

CREATE TABLE `users` (
                         `username` varchar(50) NOT NULL PRIMARY KEY,
                         `password` varchar(500) NOT NULL,
                         `enabled` boolean NOT NULL
);

CREATE TABLE `roles` (
                         `username` varchar(50) NOT NULL,
                         `role` varchar(50) NOT NULL,
                         UNIQUE INDEX `idx_user_role` (`username` ASC, `role` ASC) USING BTREE
);

CREATE TABLE `permissions` (
                               `role` varchar(50) NOT NULL,
                               `resource` varchar(255) NOT NULL,
                               `action` varchar(8) NOT NULL,
                               UNIQUE INDEX `uk_role_permission` (`role`,`resource`,`action`) USING BTREE
);

INSERT INTO users (username, password, enabled) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', TRUE);

INSERT INTO roles (username, role) VALUES ('nacos', 'ROLE_ADMIN');