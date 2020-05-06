package com.qiwen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@EnableCaching
@SpringBootApplication
@EnableConfigurationProperties
//@ComponentScans({
//        @ComponentScan(
//                excludeFilters = {
//                        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.qiwen\\.yjyx\\..+")
//                }
//        )
//    }
//)
public class Application {

    public static void main(String[] args) {
        SpringApplication springApplication= new SpringApplication(Application.class);
        springApplication.run(args);
    }
}
