package com.qiwen.base.config.db;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="PrimaryEntityManagerFactory",
        transactionManagerRef="PrimaryTransactionManager",
        basePackages= {"com.qiwen.base.repository"})
public class QWPrimaryDBConfig extends DruidDataSourceBaseConfig {

    @Primary
    @Bean(name = "PrimaryDataSourceProperties")
    @Qualifier("PrimaryDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DruidDataSourceProperties primaryDataSourceProperties() {
        return new DruidDataSourceProperties();
    }

    @SneakyThrows
    @Primary
    @Bean(name = "PrimaryDataSource")
    @Qualifier("PrimaryDataSource")
    public DataSource primaryDataSource(@Qualifier("PrimaryDataSourceProperties") DruidDataSourceProperties props) {
        return createDataSource(props);
    }

    @Primary
    @Bean(name = "PrimaryEntityManagerFactory")
    public EntityManagerFactory PrimaryEntityManagerFactoryBean(@Qualifier("PrimaryDataSource")DataSource primaryDataSource, JpaProperties jpaProperties) {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factoryBean.setDataSource(primaryDataSource);
        factoryBean.setPackagesToScan("com.qiwen.base.entity");
        Map<String, Object> hibernateProps = jpaProperties.getHibernateProperties(new HibernateSettings());
        factoryBean.setJpaPropertyMap(hibernateProps);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Primary
    @Bean(name = "PrimaryTransactionManager")
    public PlatformTransactionManager transactionManagerPrimary(@Qualifier("PrimaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
