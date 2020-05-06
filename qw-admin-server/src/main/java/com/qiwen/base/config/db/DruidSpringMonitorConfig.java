package com.qiwen.base.config.db;

import com.alibaba.druid.support.spring.stat.DruidStatInterceptor;
import com.alibaba.druid.util.Utils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.servlet.*;
import java.io.IOException;

/**
 * 开启Druid数据库监控的Spring监控
 * @author xiuchu.yang
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.druid.web-stat-filter.enabled", havingValue = "true", matchIfMissing = true)
public class DruidSpringMonitorConfig {

    @Bean
    public DruidStatInterceptor druidStatInterceptor() {
        DruidStatInterceptor dsInterceptor = new DruidStatInterceptor();
        return dsInterceptor;
    }

    @Bean
    @Scope("prototype")
    public JdkRegexpMethodPointcut druidStatPointcut() {
        JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
        pointcut.setPattern("com\\.qiwen\\.(base|yjyx)\\.service.*");
        return pointcut;
    }

    @Bean
    public DefaultPointcutAdvisor druidStatAdvisor(DruidStatInterceptor druidStatInterceptor, JdkRegexpMethodPointcut druidStatPointcut) {
        DefaultPointcutAdvisor defaultPointAdvisor = new DefaultPointcutAdvisor();
        defaultPointAdvisor.setPointcut(druidStatPointcut);
        defaultPointAdvisor.setAdvice(druidStatInterceptor);
        return defaultPointAdvisor;
    }

    /**
     * 去除Druid监控页面的广告
     */
    @Bean
    public FilterRegistrationBean removeDruidAdFilter() throws IOException {
         // 获取common.js内容
         String text = Utils.readFromResource("support/http/resources/js/common.js");
         // 屏蔽 this.buildFooter(); 直接替换为空字符串,让js没机会调用
         final String newJs = text.replace("this.buildFooter();", "");
        // 新建一个过滤器注册器对象
         FilterRegistrationBean<Filter> register = new FilterRegistrationBean<>();
         // 注册common.js文件的过滤器
        register.addUrlPatterns("/druid/js/common.js");
        register.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER - 5);
         // 添加一个匿名的过滤器对象,并把改造过的common.js文件内容写入到浏览器
        register.setFilter(new Filter() {
             @Override
             public void init(FilterConfig filterConfig) throws ServletException {

             }

             @Override
             public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                 // 重置缓冲区，响应头不会被重置
                 response.resetBuffer();
                 response.getWriter().write(newJs);
             }

             @Override
             public void destroy() {

             }
         });
        return register;
    }
}
