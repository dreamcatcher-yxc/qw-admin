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
        return CacheControl.maxAge(365, TimeUnit.DAYS);
    }

    @Bean
    public QWPrivilegeTreeSorter qwPrivilegeTreeSorter() {
        return new QWPrivilegeTreeSorter() {
            @Override
            public void sort(Privilege currentPrivilege, List<Privilege> parentPrivileges, List<Privilege> childPrivileges, List<Privilege> allPrivileges) {
                log.info("{} - {} - {}",
                        parentPrivileges.stream().map(Privilege::getName).collect(Collectors.toList()),
                        currentPrivilege.getTreeId() + "(" + currentPrivilege.getName() + ")",
                        childPrivileges.stream().map(Privilege::getName).collect(Collectors.toList())
                );
            }
        };
    }
}
