package com.qiwen.base.config;

import com.qiwen.base.util.SpringHelper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 初始化 SpringHelper
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationContextHolderConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringHelper.setApplicationContext(applicationContext);
    }
}
