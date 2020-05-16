package com.qiwen.base.config.shiro;

import com.qiwen.base.config.ApplicationContextHolderConfig;
import com.qiwen.base.util.SpringHelper;
import lombok.SneakyThrows;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限校验配置
 */
@Configuration
@AutoConfigureAfter(ApplicationContextHolderConfig.class)
public class ShiroConfig {

    /**
     * 配置 Shiro 安全管理
     *
     * @param sessionManager
     * @return
     */
    @Bean
    public SecurityManager securityManager(QWAuthRealm authRealm, QWAutoLoginRealm autoLoginRealm, SessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        ModularRealmAuthorizer authorizer = new ModularRealmAuthorizer();
        authorizer.setPermissionResolver(new WildcardPermissionResolver());
        authorizer.setPermissionResolver(new QWPermissionResolver());

        // 配置 authorizer, 只是做权限解析处理，验证操作会委托给 realm 实现
        securityManager.setAuthorizer(authorizer);

        // 配置 authenticator, 处理多个 realm 验证逻辑
        ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
        authenticator.setAuthenticationStrategy(new QWAuthenticationStrategyImpl());
        securityManager.setAuthenticator(authenticator);

        // 配置 SessionManager, 替换掉容器默认实现的 Session 管理功能, 类似于 Spring Session,
        securityManager.setSessionManager(sessionManager);
        securityManager.setRealms(Arrays.asList(authRealm, autoLoginRealm));

        // 集群需要配置成单独的缓存服务器, 这里配置成 Ehcache 实现
        securityManager.setCacheManager(securityManager.getCacheManager());

        // 自己实现了 remember, 将默认到的设置为 null
        securityManager.setRememberMeManager(null);

        return securityManager;
    }

    /**
     * 配置 Shiro 拦截器配置
     *
     * @param securityManager
     * @return
     */
    @SneakyThrows
    @Bean
    public FilterRegistrationBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 由于系统中所有的权限拦截已经通过 SpringMVC interceptor 拦截实现了, 这里将所有的请求交给 anno 处理,
        // 具体拦截由 SpringMVC interceptor 处理, 后续看情况迁移至 shiro 实现
        Map<String, String> filterChainDefinitionMap = new HashMap<>();
        filterChainDefinitionMap.put("/**", "anon");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        Object filter = shiroFilterFactoryBean.getObject();
        FilterRegistrationBean<Filter> register = new FilterRegistrationBean<>();
        register.setFilter((Filter) filter);
        register.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER - 10);
        register.addUrlPatterns("/*");
        return register;
    }

    /**
     * SessionDAO的作用是为Session提供CRUD并进行持久化的一个shiro组件
     * MemorySessionDAO 直接在内存中进行会话维护
     * EnterpriseCacheSessionDAO  提供了缓存功能的会话维护，默认情况下使用MapCache实现，内部使用ConcurrentHashMap保存缓存的会话。
     *
     * @return
     */
    private SessionDAO sessionDAO() {
        EnterpriseCacheSessionDAO enterpriseCacheSessionDAO = new EnterpriseCacheSessionDAO();
        //使用ehCacheManager
        //设置session缓存的名字 默认为 shiro-activeSessionCache
//        enterpriseCacheSessionDAO.setActiveSessionsCacheName("shiro-activeSessionCache");
        //sessionId生成器
        enterpriseCacheSessionDAO.setSessionIdGenerator(new JavaUuidSessionIdGenerator());
        return enterpriseCacheSessionDAO;
    }

    /**
     * 配置保存sessionId的cookie
     * 注意：这里的cookie 不是上面的记住我 cookie 记住我需要一个cookie session管理 也需要自己的cookie
     *
     * @return
     */
    private SimpleCookie sessionIdCookie() {
        //这个参数是cookie的名称
        SimpleCookie simpleCookie = new SimpleCookie("JSESSIONID");
        //setcookie的httponly属性如果设为true的话，会增加对xss防护的安全系数。它有以下特点：
        //setcookie()的第七个参数
        //设为true后，只能通过http访问，javascript无法访问
        //防止xss读取cookie
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        //maxAge=-1表示浏览器关闭时失效此Cookie
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }

    /**
     * 配置会话管理器，设定会话超时及保存
     *
     * @return
     */
    @Bean("sessionManager")
    public SessionManager sessionManager(
            ApplicationContext applicationContext,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") org.springframework.cache.ehcache.EhCacheCacheManager cacheManager
    ) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        //配置监听
        sessionManager.setSessionIdCookie(sessionIdCookie());
        // 设置 sessionDAO
        sessionManager.setSessionDAO(sessionDAO());
        // 配置 CacheManager, 由于系统中已经集成了 EhCache, 这里直接注入 org.springframework.cache.ehcache.EhCacheCacheManager,
        // 不要重复创建多个 CacheManager
        EhCacheManager shiroEhCacheManager = new EhCacheManager();
        shiroEhCacheManager.setCacheManager(cacheManager.getCacheManager());
        // 配置了之后会自动配置到 SessionDAO(需要 Session DAO 支持)
        sessionManager.setCacheManager(shiroEhCacheManager);
        // 配置 sessionListener
//        if (Constant.APPLICATION_CONTEXT == null) {
//            Constant.APPLICATION_CONTEXT = applicationContext;
//        }
        SpringHelper.setApplicationContext(applicationContext);
        List<SessionListener> shiroSessionListeners = SpringHelper.getRealBeansByType(SessionListener.class);
        sessionManager.setSessionListeners(shiroSessionListeners);
        // 配置 session
        Environment environment = applicationContext.getEnvironment();
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        // 10 分钟检查一遍失效 session
        sessionManager.setSessionValidationInterval(10 * 60 * 1000);

        return sessionManager;
    }

}