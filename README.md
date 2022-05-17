# guilimall

#### 介绍
#####基本功能细节介绍：
 1. 商城后台管理系统：商品属性管理，商品发布与上架，商品库存管理、订单管理、用户管理等功能；
 2. 商城本体：商城本体可以实现用户登录、商品全文索引信息查询、购物车等功能；
#####主要开发工作：
 1. 电商平台商品服务、仓储服务、检索服务、认证服务等各个微服务的业务逻辑CRUD 以及前后端联调；
 2. SpringCloud Gateway 作为分布式微服务API 网关实现路由转发等，SpringCloud Feign 完成各微服务之间的远程调用；
 3. SpringCloudAlibaba Nacos 作为配置中心和注册中心，感知各个微服务的位置，对各微服务进行健康管理，同时将各个配置
文件上传至网上，实现源码与配置的分离；
 4. 通过Docker 容器部署Mysql、Nginx、Redis、Elasticsearch 等组件，使用阿里云对象储存对图片等文件进行储存，实现服
务端签名后直传。

#### 软件架构
分布式微服务B2C 电商平台 后端开发
技术栈：SpringBoot + Mybatis-Plus + Mysql + Redis + Nginx + Nacos
链接：https://gitee.com/hylonggg/guilimall.git

#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.com/gitee-stars/)
