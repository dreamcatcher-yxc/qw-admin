package com.qiwen.base.util;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SpringHelper {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    public static ApplicationContext getApplicationContext() {
        if(applicationContext == null) {
            throw new UnsupportedOperationException("ApplicationContext 未初始化");
        }
        return applicationContext;
    }

    public static ApplicationContext getApplicationContext(ApplicationContext applicationContext) {
        if(getApplicationContext() == null) {
            setApplicationContext(applicationContext);
        }
        return getApplicationContext();
    }

    private static class LazyLoadProxyBean<T> implements MethodInterceptor {

        // 禁止 JVM 重排
        private volatile T targetObject;

        private final Class<T> clazz;

        private final String beanName;

        public LazyLoadProxyBean(Class<T> clazz, String beanName) {
            this.clazz = clazz;
            this.beanName = beanName;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (Objects.isNull(targetObject)) {
                // 代理对象懒加载部分一次只允许一个线程访问
                synchronized(this) {
                    if(Objects.isNull(targetObject)) {
                        // 首先尝试使用 name, name 没有则使用 class
                        if(StringUtils.isEmpty(beanName)) {
                            targetObject = (T) SpringHelper.getRealBean(clazz);
                        } else {
                            targetObject = (T) SpringHelper.getRealBean(beanName);
                        }
                    }
                }
            }

            Object result = method.invoke(targetObject, args);
            return result;
        }
    }

    private SpringHelper() { }

    public static <T> T getLazyBean(final Class<T> clazz) {
        return getLazyBean(clazz, null);
    }

    public static <T> T getLazyBean(final Class<T> clazz, final String beanName) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new LazyLoadProxyBean<T>(clazz, beanName));
        T proxy = (T) enhancer.create();
        return proxy;
    }

    public static <T> T getRealBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getRealBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    public static <T> List<T> getRealBeansByType(Class<T> clazz) {
        String[] beanNames = getApplicationContext().getBeanNamesForType(clazz);
        List<T> beans = Arrays.stream(beanNames)
                .map(name -> (T)getRealBean(name))
                .collect(Collectors.toList());
        return beans;
    }

    public static <T> T getProperties(String key, Class<T> targetType, T defaultValue) {
        ApplicationContext applicationContext = getApplicationContext();
        if(Objects.isNull(applicationContext)) {
            return null;
        }
        Environment environment = applicationContext.getEnvironment();
        return environment.getProperty(key, targetType, defaultValue);
    }

}
