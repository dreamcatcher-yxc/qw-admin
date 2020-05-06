package com.qiwen.base.config.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.qiwen.base.util.ReflectUtil;

import java.sql.SQLException;

public abstract class DruidDataSourceBaseConfig {

    protected DruidDataSource createDataSource(DruidDataSourceProperties props) throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(props.getDriverClassName());
        dataSource.setUrl(props.getUrl());
        dataSource.setUsername(props.getUsername());
        dataSource.setPassword(props.getPassword());
        dataSource.setMaxActive(props.getMaxActive());

        dataSource.setMinIdle(props.getMinIdle());
        dataSource.setInitialSize(props.getInitialSize());
        dataSource.setMaxWait(props.getMaxActive());
        dataSource.setTimeBetweenEvictionRunsMillis(props.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(props.getMinEvictableIdleTimeMillis());
        dataSource.setTestWhileIdle(props.isTestWhileIdle());
        dataSource.setTestOnBorrow(props.isTestOnBorrow());
        dataSource.setTestOnReturn(props.isTestOnReturn());
        dataSource.setValidationQuery(props.getValidationQuery());
        dataSource.setMaxWait(props.getMaxWait());
        dataSource.setFilters(props.getFilters());

        dataSource.setPoolPreparedStatements(props.isPoolPreparedStatements());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(props.getMaxPoolPreparedStatementPerConnectionSize());

        return dataSource;
    }

}
