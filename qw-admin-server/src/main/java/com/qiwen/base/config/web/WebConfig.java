package com.qiwen.base.config.web;

import com.qiwen.Application;
import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.config.interceptor.QWAbstractInterceptor;
import com.qiwen.base.util.SpringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@ServletComponentScan(basePackageClasses = {Application.class})
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware {

    private final QWAppConfig appConfig;

    private AnnotationConfigServletWebServerApplicationContext applicationContext;

    public WebConfig(QWAppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToDateConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] beanNames = applicationContext.getBeanNamesForType(QWAbstractInterceptor.class);

        for (String beanName : beanNames) {
            QWAbstractInterceptor interceptor = SpringHelper.getLazyBean(QWAbstractInterceptor.class, beanName);
            PathMatcher pathMatcher = interceptor.getPathMatcher();

            if(pathMatcher != null) {
                registry.addInterceptor(interceptor)
                        .addPathPatterns(interceptor.getPathPatterns())
                        .excludePathPatterns(interceptor.getExcludePathPatterns())
                        .order(interceptor.getOrder())
                        .pathMatcher(pathMatcher);
            } else {
                registry.addInterceptor(interceptor)
                        .addPathPatterns(interceptor.getPathPatterns())
                        .excludePathPatterns(interceptor.getExcludePathPatterns())
                        .order(interceptor.getOrder());
            }

            log.info("添加【{}】拦截器完成", beanName);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler(appConfig.getExternalStaticWebResourcePattern())
//                .addResourceLocations(appConfig.getExternalStaticWebResourceDir())
//                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
        List<QWAppConfig.ExternalWebResource> externalWebResources = appConfig.getExternalWebResources();
        if(CollectionUtils.isEmpty(externalWebResources)) {
            return;
        }
        externalWebResources.forEach(externalWebResource -> {
            registry.addResourceHandler(externalWebResource.getResourceHandlers())
                    .addResourceLocations(externalWebResource.getResourceLocations())
                    .setCacheControl(SpringHelper.getLazyBean(CacheControl.class, externalWebResource.getCacheControlBeanName()));
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (AnnotationConfigServletWebServerApplicationContext) applicationContext;
    }

}