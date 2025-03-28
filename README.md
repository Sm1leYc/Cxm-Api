## Cxm-API 开放平台

### 项目介绍
欢迎使用 Cxm-API 开放平台！本平台提供了接口调用和 SDK 支持，帮助开发者方便地访问和集成我们的 API 服务。

### 技术栈
- 语言：Java
- 框架：Spring Boot、Spring Cloud、MyBatis Plus
- 数据存储：Mysql、Redis
- 服务调用：Dubbo
- 熔断和降级：Sentinel

### 项目运行流程
```sh
## 克隆仓库代码
cd my-new-project
git clone https://github.com/Sm1leYc/Cxm-Api.git
```

1.  导入MAVEN依赖： mvn clean install
2. 执行 ./yuan-api/sql/ 文件夹下的sql脚本
3. 本地 nacos/conf/application.properties添加项目中的对应位置的配置项，创建nacos数据库，实现nacos配置持久化
4. 修改nacos配置中心配置文件为本地配置，以下是配置文件介绍：
    - cxm-backend-dev.yml：后端backend配置yml
    - cxm-gateway-dev.yml：网关gateway配置yml
    - cxm-gateway-sentinel-flow.yml：sentinel限流配置yml
    - cxm-gateway-sentinel-degrade.yml：sentinel熔断配置yml
    - cxm-gateway-blacklist.yml：网关请求黑名单yml
5. 启动nacos mysql redis服务
6. 启动项目，backend -> gateway -> interface





