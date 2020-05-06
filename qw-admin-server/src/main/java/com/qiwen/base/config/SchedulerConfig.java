package com.qiwen.base.config;

import com.qiwen.base.config.db.DruidDataSourceBaseConfig;
import com.qiwen.base.config.db.DruidDataSourceProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;

@Slf4j
@Configuration
public class SchedulerConfig extends DruidDataSourceBaseConfig {

    /*
     * quartz初始化监听器
     */
    @Bean
    public QuartzInitializerListener executorListener() {
        return new QuartzInitializerListener();
    }

    @Bean(name = "QwDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.quartz")
    public DruidDataSourceProperties primaryDataSourceProperties() {
        return new DruidDataSourceProperties();
    }

    @SneakyThrows
    @QuartzDataSource
    @Bean(name = "QwQuartzDataSource")
    public DataSource primaryDataSource(@Qualifier("QwDataSourceProperties") DruidDataSourceProperties props) {
        return createDataSource(props);
    }

    /*
     * 通过SchedulerFactoryBean获取Scheduler的实例
     */
    @Bean(name="Scheduler")
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws IOException {
        schedulerFactoryBean.setGlobalJobListeners();
        return schedulerFactoryBean.getScheduler();
    }
}
