> 此教程将会快速搭建一个可以正常使用的 **管理系统** 实例。

# 1、基础环境要求

- java8
- mysql5.0+
- maven3 (开发环境)
- idea or eclipse (开发环境)
- VSCode(开发环境，个人喜好)

# 2、服务端

> 工程名称: qw-admin-server

## 2.1、创建数据库

依次将 qw-admin-server 目录下的:

- `quartz_mysql.sql` quartz 数据表。

 - `qw_db_ddl_mysql.sql`  qw-admin-server 相关数据表。
 - `sys_menu_mysql.sql ` 默认菜单配置数据。
 - `sys_menu_privilege_mysql.sql` 菜单权限配置数据。
 - `yj_foo.sql` 测试模块数据库。

导入 mysql 数据库。

## 2.2、配置项目数据库连接

打开 `qw-admin-server/src/main/resources/application-dev.yml` (根据开发环境而言)，相关配置如下:

```yaml
spring:
  datasource:
    # 主数据源配置，即系统数据源
    primary:
      type: com.alibaba.druid.pool.DruidDataSource
      driver-class-Name: com.mysql.jdbc.Driver
      platform: mysql
      url: jdbc:mysql://127.0.0.1:3306/test?useSSL=false&characterEncoding=utf-8&autoReconnect=true
      username: root
      password: root
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-Millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      filters: stat,wall,slf4j
      log-slow-sql: true
    # 自定义新增模块数据库连接池配置  
    yjyx:
      type: com.alibaba.druid.pool.DruidDataSource
      driver-class-Name: com.mysql.jdbc.Driver
      platform: mysql
      url: jdbc:mysql://127.0.0.1:3306/test2?useSSL=false&characterEncoding=utf-8&autoReconnect=true
      username: root
      password: root
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-Millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      filters: stat,wall
      log-slow-sql: true
    # 定时任务数据库连接池配置  
    quartz:
      type: com.alibaba.druid.pool.DruidDataSource
      driver-class-Name: com.mysql.jdbc.Driver
      platform: mysql
      url: jdbc:mysql://127.0.0.1:3306/test?useSSL=false&characterEncoding=utf-8&autoReconnect=true
      username: root
      password: root
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-Millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      filters: stat,wall
      log-slow-sql: true
```

> 具体配置请参看相关配置文件，如需新增不同环境配置可自行添加配置文件

## 2.3、前端页面地址配置

配置如下:

```yaml
qw.constant.web:
  # 外部静态资源
  external-web-resources:
  	  # 静态资源资源匹配路径集合，ant pattern 风格
    - resource-handlers:
        - /web-admin/**
      # 资源地址
      resource-locations:
        - file:[克隆项目存放路径]/qw-admin-web/static/
      # 静态资源缓存策略配置，见 com.qiwen.yjyx.config.YJYXBeanConfig.java 下相关配置
      cache-control-bean-name: yjyxDefaultCacheControlBean
```

​     管理页面项目(**qw-admin-web**)使用了 `vue 全家桶` 、 `antd vue` 实现单页面管理系统，但是 JS 模块化加载使用了 `amd` 方式，实现框架为 `requirejs`，因此无需 webpack 打包，项目启动需要配置前端路径。

> 项目前后端不依赖模板引擎，已经进行了前后端分离，会话信息依然采用传统的 cookie + session 方式实现，如果前端项目需要和后端项目分离部署，可以将 jsessionid 作为请求参数传递即可，具体参看 Shiro 提供的 DefaultWebSessionManager.getSessionId(...) 即可知道跨域 session 解决方案。

## 2.4、通过 maven 快速启动项目

```shell
mvn spring-boot:run -Dmaven.test.skip=true -Dqw.constant.web.generate-super-user-info=true -Dqw.constant.web.super-username=admin -Dqw.constant.web.super-username=123456
```

- `qw.constant.web.generate-super-user-info=true` 启动即创建超级管理员账户，并且初始化系统必须的角色信息与权限信息，默认为 `false`
- `qw.constant.web.super-username=admin` 创建的超级管理员登录名，默认为 `true`
- `qw.constant.web.super-username=admin` 创建的超级管理员登录密码，默认为 `123456`
- `qw.constant.web.super-user-pwd=1234` 创建的超级管理员登录密码。

## 2.5、Idea 导入 IDE 启动

>本人使用 java IDE 为 idea 2019.3 ，操作系统为 windows

1、初始化 idea 项目目录结构。

```cmd
# pom 中已经加入 idea 项目目录生成插件，eclipse 未添加，eclipse 开发小伙伴可以自行添加。
# 初次导入之前在 qw-admin-server 目录下运行此命令。
mvn idea:idea
```

2、导入 idea

3、在项目下的 application-dev.yml 下添加如下配置：

```yaml
qw.constant.web:
  super-username: admin
  super-user-pwd: 123456
  generate-super-user-info: true
```

3、运行 `Application.java` 即可。

## 2.6、页面访问

打开浏览器访问 `http://localhost:8443/web-admin/index.html`即可，用户名/密码为 `admin/123456`

> 目前页面该页面在 Chrome、Firefox、360 安全浏览器(极速模式) 能够正常访问，Edge、IE 下不能正常访问。

