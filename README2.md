# RuoYi-Cloud-CICD

## 准备

1. 数据库服务器
   1. mysql
2. 数据库表
   1. ry-config
   2. ry-cloud
3. 中间件
   1. nacos
   2. redis
4. 修改项目nacos配置
   1. 数据库
      1. url
      2. username
      3. password
5. 微服务
   1. gateway
   2. auth
   3. system
## 环境变量配置

1. NEXUS_REPO_MAVEN_PUBLIC

## 镜像生成

TODO

## 团队协作开发
1. 项目已自动引入`ruoyi-common-loadbalancer`模块，引入代码在pom.xml

   > 关键实现代码在：ruoyi-common/ruoyi-common-loadbalancer/src/main/java/com/ruoyi/common/loadbalance/core/CustomSpringCloudLoadBalancer.java

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
   ...
   
       <dependencies>
   ...
            <!--自定义负载均衡(多团队开发使用)-->
           <dependency>
               <groupId>com.ruoyi</groupId>
               <artifactId>ruoyi-common-loadbalancer</artifactId>
           </dependency>
   ...
       </dependencies>
   ...
   </project>
   ```

   

2.  配置本地开发微服务的bootstrap.yml的spring.cloud.nacos.discovery.metadata.version，如

```yaml
...
spring: 
...
  cloud:
    nacos:
      discovery:
...
        metadata:
          version: 1.0.0
...
```

3. 只要发起的请求，带上header version的，则会自动负载均衡到metadata中含有version=1.0.0特定的微服务，如找不到该特定的微服务，则会请求到metadata中不含有version的微服务，请求如：

   ```
   @url_dev_gateway = http://localhost:8080
   
   ### 登入 - 无version
   POST {{url_dev_gateway}}/auth/login HTTP/1.1
   content-type: application/json
   
   {
     "username": "admin",
     "password": "admin123"
   }
   
   ### 登入 - version:1.0.0
   POST {{url_dev_gateway}}/auth/login HTTP/1.1
   content-type: application/json
   version: 1.0.0
   
   {
     "username": "admin",
     "password": "admin123"
   }
   
   ### 登入 - version:1.0.1
   POST {{url_dev_gateway}}/auth/login HTTP/1.1
   content-type: application/json
   version: 1.0.1
   
   {
     "username": "admin",
     "password": "admin123"
   }
   ```

   