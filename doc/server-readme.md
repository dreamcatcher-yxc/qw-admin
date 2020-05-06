# QW-Admin-Server

# 一、项目目录结构

- data - 项目外部数据目录，开发过程无用

- doc - 项目文档目录，开发过程无用

- run - 项目启动相关文件

- sql - 项目启动初始化相关 sql 脚本

- src - 源代码路径

  - `main.java.com.qiwen.base` - 项目核心模块，该模块为顶层模块，只能被其他业务模块引入，不能引入其他业务模块相关代码。
    - `config` - 项目配置路径
      - `annotaion` - 自定义注解目录
      - `db` - 基础数据库配置目录
      - `job` - 基础 quartz 相关配置目录
      - `listeners` - 基础监听器配置相关路径
      - `log` - 日志切面配置相关路径
      - `shiro` - shiro 相关配置
      - `thymeleaf` - thymeleaf 配置相关目录
      - `web` - web 相关配置
      - `ApplicationContextHolderConfig.java` - web 相关配置
      - `QWAppConfig.java` - 基础扩展配置项
      - `QWConstant.java` - 全局常量
      - `SchedulerConfig.java` - quartz 配置
    - `controller`
    - `dto` - DTO 类目录
    - `entity` - JPA Entity
    - `exception` - 自定义异常
    - `util` - 系统工具类
    - `vo` - VO 类目录

  - `main.java.com.qiwen.yjyx` - 扩展业务模块目录
    - `config`
    - `controller`
    - `dto`
    - `entity`
    - `mapper`
    - `service`
  - `main.java`
    - `Application.java`-  项目启动入口
  - `main.resources`- 项目资源配置路径
    - mapper 
    - static
    - templates
    - application.yml
    - application-dev.yml
    - application-test.yml
    - application-prd.yml
    - ehcache.yml
    - logback-spring.xml
    - message.properties
  - `test`- 测试文件目录

# 二、权限控制

## 1、背景介绍

​	本系统只做了功能权限控制，采用了 RBAC 模型，粒度达到接口级别，在实现之初并未采用权限框架，后期集成了 Shiro，为了与原系统权限设计相兼容，在 Shiro 配置上做了如下配置修改：

- 自定义 `QWPermissionProxy` 类实现功能权限校验，功能权限校验最终代理给 `AuthServiceImpl` 校验。
- `登录逻辑`、`自动登录逻辑`自定义实现。
- 自定义`QWAuthenticationStrategyImpl`  实现多 `Realm` 认证与权限校验。
- 功能权限名称采用 `-` 分割（区别于 Shiro 采用 `:` 分割风格）。

> Shiro 风格权限表达式目前系统 Realm 未支持，使用不会抛出异常，但是结果都是 false。

## 2、Shiro 权限注解支持

> 权限注解在 @Controller 方法上有效，见 com.qiwen.base.config.interceptor.QWAbstractAuthInterceptor.hasShiroAuthcAnnotation() 方法实现逻辑。

| 注解名称                | 描述                                                         |
| :---------------------- | ------------------------------------------------------------ |
| @RequiresPermissions    | 标识访问该接口所需的权限集                                   |
| @RequiresRoles          | 标识访问该接口所需的角色集                                   |
| @RequiresAuthentication | 标识访问该接口需要已登录，并且是通过非自动登录方式登录的系统 |
| @RequiresUser           | 标识访问该接口需要已登录                                     |
| @RequiresGuest          | 标识访问该接口不能已登录，例如: 访问登录接口                 |
| @Desc   (自定义)        | 标识访问该接口所需的权限集，日志配置，功能权限元数据         |

## 3、权限注解使用示例

### 3.1、 Shiro 原生注解

```java
@RestController
@RequestMapping("/foo")
public static class FooController {
    
    // 需要 foo1 & foo2 权限
    // @RequiresPermissions({"foo1", "foo2"})
    @RequiresPermissions("foo1 & foo2")
    public String testRequiresPermissionOfLogicAnd() {
        // do something...
    }
    
    // 需要 foo1 | foo2 权限
    // @RequiresPermissions(value={"foo1", "foo2"}, logical=Logical.OR)
    @RequiresPermissions("foo1 | foo2")
    public String testRequiresPermissionOfLogicOr() {
        // do something...
    }
    
    // 需要 foo1 & foo2 角色，'或者' 逻辑不做赘述
    // @RequiresRoles({"foo1", "foo2"})
    @RequiresPermissions("foo1 | foo2")
    public String testRequiresPermission() {
     	// do something...   
    }
    
    // @RequiresAuthentication、@RequiresUser、@RequiresGuest 使用比较简单，不做说明
}
```

### 3.2 系统扩展注解 @Desc 

```java
/**
 * 父级菜单权限名称为 user-admin，提供为权限归组功能。
 */
@Desc(groupName="user-admin")
@RestController
@RequestMapping("/foo")
public static class FooController {
    
    /**
     * 需要 user-add 权限，权限描述为: 添加用户，该信息将会自动添加进数据库。
     * 注意: 当我们知道自己定义的权限名需要自定新增入系统找中，可以使用此注解，
     * 其他地方可以通过 @RequiresPermissions("user-add") 使用此功能权限。
     */
    @Desc(name="user-add", value="添加用户")
    public String addUser() {
        // do something...
    }
}
```

如上配置，将会生成如下属性权限结构：

```powershell
user-admin
	|---user-add
	|---...
```

多层级权限树

```java
/**
 * 父级菜单权限名称为 user-admin，提供为权限归组功能。
 */
@Desc(name="base-system > user-admin")
@RestController
@RequestMapping("/foo")
public static class FooController {
    
    /**
     * 需要 user-add 权限，权限描述为: 添加用户，该信息将会自动添加进数据库。
     * 注意: 当我们知道自己定义的权限名需要自定新增入系统找中，可以使用此注解，
     * 其他地方可以通过 @RequiresPermissions("user-add") 使用此功能权限。
     */
    @Desc(name="user-add", value="添加用户")
    public String addUser() {
        // do something...
    }
}
```

如上配置，将会生成如下属性权限结构：

```powershell
base-system
	|---user-admin
            |---user-add
            |---...
```



## 4、自动生成权限树

在开发过程中，通过 `@Desc` 注解定义功能权限元信息，可以通过 `com.qiwen.base.service.impl.PrivilegeServiceImpl#generatePrivilegeInfo` 生成，项目启动之后进入角色/权限管理，点击`重新生成权限树`按钮即可生成。

## 5、权限树排序

`com.qiwen.yjyx.config.YJYXBeanConfig#qwPrivilegeTreeSorter`默认实现了一个待权限树排序的排序器，

`com.qiwen.base.config.db.QWPrivilegeTreeSorter#sort`会在权限树已经生成，未保存进数据库之前被调用，该方法每次将会在每一个节点被遍历的时候调用，可以通过修改 `childPrivileges`参数的 `orderIndex`属性达到为树节点排序的目的。 

> `com.qiwen.base.config.db.QWPrivilegeTreeSorter` Bean 全局只允许有一个，有多个存在请使用 `@Primary` 指定默认实现。

## 6、用户、角色、权限管理配置

### 6.1、新增用户

比较简单，略

### 6.2、分配角色

比较简单，略

### 6.3、新增角色

比较简单，略

### 6.4、分配权限

比较简单，略

### 6.5、菜单/权限管理

用户拥有角色，角色拥有权限，菜单叶子节点分配权限之后，能够根据用户拥有的权限计算出当前需要展示的菜单节点。

# 三、日志

>  日志拦截通过 AOP 实现，具体参看 `com.qiwen.base.config.log.DefaultLoggerAspectHandler` 实现



```java
/**
 * 日志示例
 */
@Desc(logGroupName="用户管理")
@RestController
@RequestMapping("/user/rest")
public static class UserController {
    
    /**
     * log 指示日志描述信息
     * logIncludeParams 指定只记录数组包含名称的参数
     * logExcludeParams 指定只记录数组不包含名称的参数
     * 注意: logIncludeParams 与 logExcludeParams 为互斥关系, logIncludeParams 优先级最高，
     * 既两者同时出现 logIncludeParams 有效。
     */
    // @Desc(log="添加用户", logExcludeParams={"password"})
    @Desc(log="添加用户", logIncludeParams={"username"})
    @PostMapping("/add");
    public String addUser(String username, String password) {
        // do something...
    }
    
    @Desc(log="删除用户")
    @PostMapping("/delete");
    public String addUser(String userId) {
        // do something...
    },
    
    /**
     * customLog=true 表示需要自定义日志内容
     */
    @Desc(customLog=true, logIncludeParams={"fooKey"}/*一个参数都不记录*/)
    public String modifyPasswod(String srcPassword, String newPassword) {
        // 必须指定 customLog 未 true, 否则此属性未 null
        // operateLog 中已经包含了需要记录的日志信息, 可以做修改
        OperateLog operateLog = (OperateLog) request.getAttribute(Desc.CUSTOM_ATTR_NAME);
        operateLog.setOperation(SystemUtil.currentLoginUser().getUsername() + "修改了密码");
    	// 自己的业务逻辑
    }
}
```

如上配置，生成的日志描述信息为: 

```javascript
{
    "operation": "用户管理 > 添加用户",
    "operateParam": { "username": "..." }
    //其余数据参看 com.qiwen.base.entity.OperateLog 类定义 
}


{
    "operation": "用户管理 > 删除用户",
    "operateParam": { "userId": "..." }
}    
```

# 四、@Desc 注解的综合使用

见 `com.qiwen.base.controller.admin.base.*Controller.java` 相关使用。

# 五、定时任务

> 系统集成 `spring-boot-starter-quartz`，数据源采用 `druid`，实现了简单的运行时动态 **新建**、**删除**、**触发**、**暂停**任务功能。

## 1、定义任务

```java
package com.qiwen.yjyx.config.job;

import com.qiwen.base.entity.WorkLog;
import com.qiwen.base.service.IWorkLogService;
import com.qiwen.base.util.SpringHelper;
import com.qiwen.yjyx.config.YJYXAppConfig;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

@Slf4j
public class YJFooJob implements Job {

    public static final String NAME = "测试任务";

    private final YJYXAppConfig appConfig;

    private final IWorkLogService workLogService;

    public YJFooJob() {
        // 注入 bean
        this.appConfig = SpringHelper.getRealBean(YJYXAppConfig.class);
        this.workLogService = SpringHelper.getRealBean(IWorkLogService.class);
        // 通过 SpringHelper.getProperties(...) 获取配置属性
        // 通过 org.apache.commons.lang3.SystemUtils 获取 JVM 系统相关属性
        // ...
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date startDate = new Date();
        // do something()
        Date endDate = new Date();
		
        // 保存工作日志可选, 可以提取到一个抽线类...
        WorkLog workLog = new WorkLog();
        workLog.setStartDate(startDate);
        workLog.setEndDate(endDate);
        workLog.setDescription("这是一段描述信息");
        JobDetail jobDetail = context.getJobDetail();
        String jobName = jobDetail.getKey().getName();
        String jobGroupName = jobDetail.getKey().getGroup();
        workLog.setGroupName(jobGroupName);
        workLog.setName(jobName);
        workLog.setJobClass(jobDetail.getJobClass().getName());
        workLog.setStatus(1);
        workLogService.save(workLog);
    }
}

```

## 2、自定义任务扫描配置

```yaml
qw.constant.web:
  # 配置 com.qiwen.base.config.job、com.qiwen.yjyx.config.job 包下的 Job 子类都会
  # 被添加到可选新增任务列表中
  quartz-job-scan-packages:
    - com.qiwen.base.config.job
    - com.qiwen.yjyx.config.job
```



完成上述操作之后，在 **任务管理** > **新增任务** 中会自动出现 **测试任务** 选项。

> **<font color="red">每个 Job 只能存在一个。</font>**



# 六、 扩展Spring Boot快速配置项

## 1、配置外部静态资源

通常前后端分离项目静态资源为了访问性能，都放在单独的静态资源服务器上面，java 应用服务器处理静态资源并非是强项，系统为了方便集成简单配置外部静态资源文件快速访问选项，配置如下:

```yaml
qw.constant.web:
  # 配置2个外部静态资源路径
  external-web-resources:
  	# ${qw-global-base-dir}www/ 下的静态资源 可以通过访问 /yjyxapp/** 获取
    - resource-handlers:
        - /yjyxapp/**
      resource-locations:
        - file:${qw-global-base-dir}www/
      # 缓存配置 Bean 名称
      cache-control-bean-name: yjyxDefaultCacheControlBean
    # 原理同上...
    - resource-handlers:
        - /web-admin/**
      resource-locations:
        - file:D:/data/YGWL_COMPANY/qw-admin/qw-admin-web/static/
      cache-control-bean-name: yjyxDefaultCacheControlBean
```

`yjyxDefaultCacheControlBean`  配置

```java
package com.qiwen.yjyx.config;

import com.qiwen.base.config.db.QWPrivilegeTreeSorter;
import com.qiwen.base.entity.Privilege;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class YJYXBeanConfig {

    @Bean(name = "yjyxDefaultCacheControlBean")
    public CacheControl yjyxDefaultCacheControlBean() {
        // 缓存 365 天，使用说明见 SringMVC 文档说明。
        return CacheControl.maxAge(365, TimeUnit.DAYS);
    }
}

```

## 2、快速配置 SpringMVC 拦截器

```java
/**
 *
 * @author yangxiuchu
 */
@Slf4j
@Component
public class FooPInterceptor extends QWAbstractInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // do something
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
       // do something
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // do something
    }

    /**
     * 配置拦截路径
     */
    @Override
    public String[] getPathPatterns() {
        String[] patterns = { "/yjyx/admin/v1/**" };
        return patterns;
    }
    
    /**
     * 配置拦截器顺序，默认为 0
     */
    @Override
    public int getOrder() {
        return super.getOrder();
    }
}

```

系统中 `com.qiwen.base.config.interceptor.QWAbstractInterceptor`子类 Bean 都会被自动注册为 Spring MVC 拦截器。

## 3、图片存储

系统提供了简单的基于本地文件系统存储文件的功能，见 `com.qiwen.base.controller.admin.AdminImgRestController`。





