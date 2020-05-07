# qw-admin

# 一、介绍

基于 Spring Boot、Shiro 的快后台管理系统快速开发模板。

> 现阶段未单体项目，后期考虑采用 java9 之后的模块化拆分系统。

# 二、软件架构

- 后端技术
  - Spring Boot 2.0.0
  - Apache Shiro
  - Spring Data JPA
  - Mybatis Plus
  - Thymeleaf
  - Ehcache
  - ...
- 前端技术
  - requirejs
  - vue.js(2+)
  - vue-router
  - vue-store
  - jquery
  - antd-vue
  - antd-vue-pro
  - lodash

> **注意: 前端项目发布不用打包**

# 三、安装教程

[快速启动教程](./doc/quickly-start.md)

# 四、使用说明

## 1、后端

参看 [qw-admin-server使用说明](doc/server-readme.md)

## 2、前端

参看 [qw-admin-web使用说明](doc/client-readme.md)

​	

# 五、效果预览

![1](./doc/imgs/1.png)

![2](doc/imgs/2.png)


![3](doc/imgs/3.png)


![4](doc/imgs/4.png)


![5](doc/imgs/5.png)


![6](doc/imgs/6.png)


![6-1](doc/imgs/6-1.png)


![6-2](doc/imgs/6-2.png)


![7](doc/imgs/7.png)


![8](doc/imgs/8.png)


![8](doc/imgs/9.png)


![10](doc/imgs/10.png)


![11](doc/imgs/11.png)


![12](doc/imgs/12.png)


![13](doc/imgs/13.png)


![14](doc/imgs/14.png)


![15](doc/imgs/15.png)


![16](doc/imgs/16.png)


![17](doc/imgs/17.png)



# 六、后续开发计划

- 后端
  - 简单的部门管理功能
  - 添加登陆密码重试错误次数限制功能，并且支持动态配置。
  - 集成限流功能。
  - 工具类替换为 hutool。
  - 项目拆分 maven 聚合工程。
- 前端
  - vue 文件预编译。
  - requirejs 模块添加合并压缩配置。

> 暂时无打算支持 IE，后管页面支持 360（极速模式）、firefox、chrome 浏览器。

# 七、参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
5.  有问题请评论区留言

