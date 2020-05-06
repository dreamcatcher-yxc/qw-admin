package com.qiwen.yjyx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("yjyx")
public class YJYXAppConfig {

    private String fooVal1;

    private String fooVal2;

}
