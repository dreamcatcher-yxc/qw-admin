package com.qiwen.yjyx.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.incrementer.OracleKeyGenerator;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.qiwen.base.config.db.DruidDataSourceBaseConfig;
import com.qiwen.base.config.db.DruidDataSourceProperties;
import lombok.SneakyThrows;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan("com.qiwen.yjyx.mapper")
public class YJYXDBConfig extends DruidDataSourceBaseConfig {

    @Autowired
    @Qualifier("YJYXDataSource")
    private DataSource dataSource;

    @Bean(name = "YJYXDataSourceProperties")
    @Qualifier("YJYXDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.yjyx")
    public DruidDataSourceProperties primaryDataSourceProperties() {
        return new DruidDataSourceProperties();
    }

    @SneakyThrows
    @Bean(name = "YJYXDataSource")
    @Qualifier("YJYXDataSource")
    public DataSource YJYXDataSource(@Qualifier("YJYXDataSourceProperties") DruidDataSourceProperties props) {
        return createDataSource(props);
    }

    private PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        return paginationInterceptor;
    }

    @Bean(name = "YJYXEntityManagerFactory")
    public SqlSessionFactory YJYXEntityManagerFactory() throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        sqlSessionFactory.setConfiguration(configuration);
        //PerformanceInterceptor(),OptimisticLockerInterceptor()
        //添加分页功能
        sqlSessionFactory.setPlugins(new Interceptor[]{paginationInterceptor()});
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactory.setMapperLocations(resolver.getResources("classpath:/mapper/yjyx/*Mapper.xml"));
        sqlSessionFactory.setTypeAliasesPackage("com.qiwen.yjyx.entity");
        // GlobalConfig
//        GlobalConfig globalConfig = new GlobalConfig();
//        globalConfig.setId
//        sqlSessionFactory.setGlobalConfig();
        return sqlSessionFactory.getObject();
    }

    @Bean
    public GlobalConfig globalConfiguration() {
        GlobalConfig conf = new GlobalConfig();
        conf.setDbConfig(new GlobalConfig.DbConfig().setKeyGenerator(new OracleKeyGenerator()));
        return conf;
    }

    @Bean(name = YJYXConstant.TRANS_MANAGER_NAME)
    public PlatformTransactionManager transactionManagerPrimary(EntityManagerFactoryBuilder builder) {
        return new DataSourceTransactionManager(dataSource);
    }

}
